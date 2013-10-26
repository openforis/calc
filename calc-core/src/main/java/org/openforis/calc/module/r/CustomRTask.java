package org.openforis.calc.module.r;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.CalculationStepTask;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.DataRecordVisitor;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.EntityDataViewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Runs a user-defined R statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomRTask extends CalculationStepTask {

	@JsonIgnore
	private List<DataRecord> results;

	@JsonIgnore
	@Autowired
	private R r;

	@JsonIgnore
	private long maxItems;
	@JsonIgnore
	private int limit;

	@Autowired
	private EntityDataViewDao viewDao;

	// used for execution
	@JsonIgnore
	private REnvironment rEnvironment;
	@JsonIgnore
	private Set<String> variables;
	@JsonIgnore
	private List<Query> updates;

	public CustomRTask() {
		limit = 5000;
		maxItems = -1;
	}

	@Override
	@Transactional
	synchronized protected void execute() throws RException, InterruptedException {
		rEnvironment = r.newEnvironment();
		variables = getCalculationStep().getInputVariables();
		// updates to run in batch
		updates = new ArrayList<Query>();

		// for output
		results = new Vector<DataRecord>();

		// reset output variable
		Query resetOutput = getResetOutputUpdate();
		updates.add(resetOutput);

		// prepare the select statement
		// SelectQuery<Record> selectQuery = getSelectStatement(variables);

		long iterations = Math.round(Math.ceil(getTotalItems() / (double) limit));

		for (int i = 0; i < iterations; i++) {
			// select records for the current iteration
			int offset = limit * i;
			int numberOfRows = (int) ((getItemsRemaining() < (offset + limit)) ? getItemsRemaining() : limit);

			// List<DataRecord> records =
			viewDao.query(this.new RTaskVisitor(), getWorkspace(), offset, numberOfRows, getEntity().getName(), variables.toArray(new String[] {}));

			// selectQuery.addLimit(offset, numberOfRows);
			// System.out.println(selectQuery.toString());
			// Result<Record> records = selectQuery.fetch();
			// execute the script for each record
			// for (Record record : records) {
			// Query update = executeScript(rEnvironment, variables, record);
			// updates.add(update);
			// }

			// execute the sql updates in batch
			psql().batch(updates).execute();
			// clear batch queries
			updates.clear();
		}
	}

	/**
	 * Execute the R script, and returns the update statement for the current
	 * record
	 * 
	 * @param rEnvironment
	 * @param variables
	 * @param table
	 * @param outputVariable
	 * @param outputField
	 * @param record
	 * @return
	 * @throws RException
	 * @throws InterruptedException
	 */
	// private Query executeScript(REnvironment rEnvironment, Set<String>
	// variables, Record record) throws RException, InterruptedException {
	// DataTable dataTable = getDataTable();
	// Variable<?> outputVariable = getOutputVariable();
	// Field<Double> outputField = getOutputField();
	// String script = getScript();
	// Long id = record.getValue(dataTable.getIdField().getName(), Long.class);
	//
	// DataRecord dataRecord = new DataRecord(id);
	//
	// for (String var : variables) {
	// Object value = record.getValue(var);
	// script = script.replaceAll("\\$" + var + "\\$", value.toString());
	// dataRecord.add(var, value);
	// }
	//
	// double result = rEnvironment.evalDouble(script);
	// dataRecord.add(outputVariable.getName(), result);
	//
	// results.add(dataRecord);
	//
	// incrementItemsProcessed();
	//
	// Query update = psql().update(dataTable).set(outputField,
	// result).where(dataTable.getIdField().eq(id));
	// return update;
	// }
	//
	// private SelectQuery<Record> getSelectStatement(Set<String> variables) {
	// DataTable table = getDataView();
	// SelectQuery<Record> selectQuery = psql().selectQuery();
	// selectQuery.addSelect(table.getIdField());
	//
	// for (String var : variables) {
	// selectQuery.addSelect(table.field(var));
	// }
	// selectQuery.addFrom(table);
	// selectQuery.addOrderBy(table.getIdField());
	// return selectQuery;
	// }

	private Query getResetOutputUpdate() {
		DataTable table = getDataTable();
		Field<Double> outputField = getOutputField();

		Query update = psql().update(table).set(outputField, DSL.val(null, Double.class));

		return update;
	}

	@Override
	protected long countTotalItems() {
//		DataTable table = getDataView();
		long count = this.viewDao.count(getEntity());
//				psql().selectCount().from(table).fetchOne(0, Long.class);
		if ( maxItems <= 0 ||  maxItems > count ) {
			maxItems = count;
		}
		
		return maxItems;
	}

	@SuppressWarnings("unchecked")
	private Field<Double> getOutputField() {
		DataTable table = getDataView();
		Variable<?> outputVariable = getOutputVariable();
		return (Field<Double>) table.field(outputVariable.getName());
	}

	private Variable<?> getOutputVariable() {
		return getCalculationStep().getOutputVariable();
	}

	private DataTable getDataView() {
		Entity entity = getEntity();
		EntityDataView table = getJob().getInputSchema().getDataView(entity);
		return table;
	}

	private DataTable getDataTable() {
		Entity entity = getEntity();
		DataTable table = getJob().getInputSchema().getDataTable(entity);
		return table;
	}

	private Entity getEntity() {
		Entity entity = getOutputVariable().getEntity();
		return entity;
	}

	private String getScript() {
		return getCalculationStep().getScript();
	}

	// not used for now
	synchronized protected void executeExternalScript() throws RException {
		REnvironment env = r.newEnvironment();
		String script = getCalculationStep().getScript();
		log().debug("Custom R: " + script);
		env.eval(script);
	}

	public long getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(long max) {
		this.maxItems = max;
	}
	
	@JsonIgnore
	List<DataRecord> bufferResults = null;
	@JsonIgnore
	public List<DataRecord> getResults(int from , int to){
		if (results != null) {
		synchronized (results) {
			bufferResults = new ArrayList<DataRecord>();
			if( from + (to - from) > results.size() ) {
				to = results.size();
			}
			
			List<DataRecord> subList = results.subList(from, to);
//			 bufferResults =C ollectionUtils.unmodifiableList(subList);
			bufferResults.addAll(subList);
			 return bufferResults;
		}
		} 
		return null;
	}
	
	@JsonIgnore
	public DataRecord getNextResult() {
		synchronized (results) {
			if (results.isEmpty()) {
				return null;
			}
			DataRecord record = results.get(0);
			results.remove(0);
			return record;
		}
	}

	// TODO remove ? old buffered results
//	
//
//	public List<DataRecord> getBufferedResults() {
//		return bufferResults;
//	}
//
//	// @JsonIgnore
//	public void prepareBufferedResults() {
//		if (results != null) {
//			synchronized (results) {
//				bufferResults = new ArrayList<DataRecord>(results);
//				results.clear();
//
//			}
//		}
//	}

	private class RTaskVisitor implements DataRecordVisitor {

		@Override
		public void visit(DataRecord record) {
			Variable<?> outputVariable = getOutputVariable();
			Field<Double> outputField = getOutputField();
			String script = getScript();

			for (String var : variables) {
				Object value = record.getValue(var);
				script = script.replaceAll("\\$" + var + "\\$", value.toString());
				// record.addField(var, value);
			}

			double result = 0;
			try {
				result = rEnvironment.evalDouble(script);
				record.add(outputVariable.getName(), result);
			} catch (RException e) {
				throw new IllegalStateException("Error while evaluating script: " + script);
			}

			results.add(record);

			incrementItemsProcessed();

			// update query
			DataTable dataTable = getDataTable();
			Long id = record.getId();
			Query update = psql().update(dataTable).set(outputField, result).where(dataTable.getIdField().eq(id));
			// return update;
			updates.add(update);

		}
	}

}

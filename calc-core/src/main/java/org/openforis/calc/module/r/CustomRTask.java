package org.openforis.calc.module.r;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Row1;
import org.jooq.SelectQuery;
import org.jooq.UpdateConditionStep;
import org.openforis.calc.engine.CalculationStepTask;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.schema.InputTable;
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

	private static final String ID = "id";
	private static final String VARIABLE_PLACEMARK = "\\$(.+?)\\$";
	
	@JsonIgnore
	@Autowired
	private R r;

	@JsonIgnore
	private long maxItems;
	@JsonIgnore
	private int limit;
	
	private Map<String, Object> results; 
	
	/**
	 * semaphore used to avoid not synchronized access to the results;
	 */
	private Object _results_semaphore;
	
	public CustomRTask() {
		limit = 5000;
		maxItems = -1;
		_results_semaphore = new Object();
	}
	
	@Override
	@Transactional
	synchronized
	protected void execute() throws RException {
		REnvironment rEnvironment = r.newEnvironment();
		Set<String> variables = extractVariables();
		InputTable table = getTable();
		
		//for output
		results = new HashMap<String, Object>();
		Variable<?> outputVariable = getOutputVariable();
		@SuppressWarnings("unchecked")
		Field<Double> outputField = (Field<Double>) table.field(outputVariable.getName());
		
		//prepare the select statement
		SelectQuery<Record> selectQuery = psql().selectQuery();
		selectQuery.addSelect(table.getIdField().as(ID));
		for (String var : variables) {
			selectQuery.addSelect(table.field(var));
		}
		selectQuery.addFrom(table);
		
		
		
		
		long iterations = Math.round( Math.ceil(getTotalItems() / (double)limit) );		
		long start = System.currentTimeMillis();
		System.out.println("==== START");
		
		List<Query> updates = new ArrayList<Query>();
		for (int i = 0; i < iterations; i++) {
			System.out.println("==== ITERATION: " + i);
			
			int offset = limit * i;
			selectQuery.addLimit(offset, limit);
			Result<Record> records = selectQuery.fetch();

			// execute the script for each record
			for (Record record : records) {
				String script = getScript();
				synchronized (_results_semaphore) {
					Integer idValue = record.getValue(ID, Integer.class);
					results.put(ID, idValue);
					for (String var : variables ) {
						Object variableValue = record.getValue(var);
						script = script.replaceAll("\\$"+var+"\\$", variableValue.toString());
//						System.out.println(script);
						results.put(var, variableValue);						
					}
					double result = rEnvironment.evalDouble(script);
					results.put(outputVariable.getName(), result);
//					System.out.println(result);
					incrementItemsProcessed();
					
					
					Query update = psql()
									.update(table)
									.set(outputField, result)
									.where( table.getIdField().eq(idValue) );
					updates.add(update);
				}
			}
			
			//execute the updates in batch
			psql()
				.batch(updates)
				.execute();
			//clear batch queries
			updates.clear();
		}
		System.out.println("==== END in " + (System.currentTimeMillis() - start) +"ms. Items processed: " + getItemsProcessed());
	}
	
	@Override
	protected long countTotalItems() {
		if( maxItems <= 0 ) {
			InputTable table = getTable();
			
			maxItems = psql()
							.selectCount()
							.from(table)
							.fetchOne(0, Long.class);
			
		}
		return maxItems;
	}

	private Variable<?> getOutputVariable() {
		return getCalculationStep().getOutputVariable();
	}

	private InputTable getTable() {
		Entity entity = getCalculationStep().getOutputVariable().getEntity();
		InputTable table = getJob().getInputSchema().getDataTable(entity);
		return table;
	}

	private String getScript() {
		return getCalculationStep().getScript();
	}
	
	private Set<String> extractVariables() {
		Set<String> variables = new HashSet<String>();
		Pattern p = Pattern.compile(VARIABLE_PLACEMARK);
		Matcher m = p.matcher(getScript());
		while(m.find()) {
			String variable = m.group(1);
			variables.add(variable);
		}
		return variables;
	}
	
	// not used for now
	synchronized
	protected void executeExternalScript() throws RException {
		REnvironment env = r.newEnvironment();
		String script = getCalculationStep().getScript();
		log().debug("Custom R: "+script);
		env.eval(script);
	}
	
	public long getMaxItems() {
		return maxItems;
	}
	
	public void setMaxItems(long max) {
		this.maxItems = max;
	}

//	public int getLimit() {
//		return limit;
//	}
//	
//	public void setLimit(int offset) {
//		this.limit = offset;
//	}
	
	public static void main(String[] args) {
		CustomRTask r = new CustomRTask();
		System.out.println(r.getMaxItems());
		
		String s = "if($a$ == $b$) / $aaaa$";
//		String s = "if({a} == {b}) / {c}";
		Pattern p = Pattern.compile(VARIABLE_PLACEMARK);
		Matcher m = p.matcher(s);
		while(m.find()) {
		    System.out.print("found "+  m.group(1));
		    System.out.println();
		}
//		int a = 7345;
		int a = 1500;
		int b = 5000;
		
		System.out.println( Math.round( Math.ceil((a / (double)b))) );
		
//		int i = m.groupCount();
//		String g = m.group(0);
//		System.out.println(g);
//		System.out.println(i);
//		System.out.println(m);
	}
}

/**
 * 
 */
package org.openforis.calc.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.psql.AlterTableStep.AlterColumnStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.calc.r.DbConnect;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.RVector;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class CalcJob extends Job {

	@Autowired
	@JsonIgnore
	R r;
	@JsonIgnore
	private REnvironment rEnvironment;
	
	@JsonIgnore
//	private List<CalculationStep> calculationSteps;
	private Map<Integer, List<CalculationStep>> calculationSteps;
	
	@Autowired
	@JsonIgnore
	private BeanFactory beanFactory;
	
	
	//TODO read dynamically these properties 
	String host = "localhost";
	String database = "calc";
	String user = "calc";
	String password = "calc";
	int port = 5432;
	
	/**
	 * @param workspace
	 * @param dataSource
	 */
	protected CalcJob(Workspace workspace, DataSource dataSource, BeanFactory beanFactory) {
		super(workspace, dataSource);
		setSchemas(new Schemas(workspace));
		
		this.beanFactory = beanFactory;
		this.calculationSteps = new  HashMap<Integer, List<CalculationStep>>();
	}
	
	// calculation steps are grouped by entity for performance reason
	public void addCalculationStep(CalculationStep step) {
		Integer entityId = step.getOutputVariable().getEntity().getId();
		List<CalculationStep> steps = this.calculationSteps.get(entityId);
		if( steps == null ) {
			steps = new ArrayList<CalculationStep>();
			this.calculationSteps.put(entityId, steps);
		}
		steps.add(step);
	}
	
	public void addCalculationStep(List<CalculationStep> steps){
		for (CalculationStep calculationStep : steps) {
			addCalculationStep(calculationStep);
		}
	}
	
	@Override
	public void init() {
		try {
			this.rEnvironment = r.newEnvironment();
		} catch (RException e) {
			throw new CalculationException("Unable to create rEnvironement", e);
		}
		
		initTasks();
		
		super.init();
	}
	
	@SuppressWarnings("unchecked")
	private void initTasks() {
		
		// init task
		CalcRTask initTask = createTask("Open database connection");
		
		// init libraries
		initTask.addScript( r().library("lmfor") );
		initTask.addScript( r().library("RPostgreSQL") );
		
		// create driver
		RVariable driver = r().variable("driver");
		initTask.addScript( r().setValue(driver, r().dbDriver("PostgreSQL")) );
		
		// open connection
		RVariable connection = r().variable("connection");
		DbConnect dbConnect = r().dbConnect(driver, host, database, user, password, port);
		initTask.addScript( r().setValue(connection, dbConnect) );
		
		// set search path to current schema
		// TODO replace with psql().setSearchPath() once implemented
		initTask.addScript( r().dbSendQuery(connection, "set search_path to " + getInputSchema().getName() +", public") );
		addTask(initTask);

		
		// execute the calculation steps grouped by entity
		
		for (Integer entityId : this.calculationSteps.keySet()) {
			Entity entity = getWorkspace().getEntityById(entityId);
			EntityDataView view = getSchemas().getInputSchema().getDataView(entity);
			InputTable table = getSchemas().getInputSchema().getDataTable(entity);
			Field<?> primaryKeyField = view.getPrimaryKey().getFields().get(0);
			String primaryKey = primaryKeyField.getName();
			RVariable dataFrame = r().variable( entity.getName() );
			
			
			// create calc steps
			List<CalculationStepRTask> calculationStepTasks = new ArrayList<CalculationStepRTask>();
			Set<String> outputVariables = new HashSet<String>();
			Set<String> inputVariables = new HashSet<String>();
			
			// plot area script if available
			RScript plotArea = null;
			String plotAreaScript = entity.getPlotAreaScript();
			if( StringUtils.isNotBlank(plotAreaScript) ){
				inputVariables.addAll( extractVariables(plotAreaScript) );
				plotAreaScript = replaceVariables(dataFrame, plotAreaScript);
				plotArea = r().rScript( plotAreaScript );
			}
			
			// create a task for each step
			for ( CalculationStep step : this.calculationSteps.get(entityId) ) {
				CalculationStepRTask task = new CalculationStepRTask(rEnvironment, dataFrame, step, plotArea);
				calculationStepTasks.add(task);
				
				outputVariables.addAll(task.getOutputVariables());
				inputVariables.addAll(task.getInputVariables());
			}
			
			// ===== read data task
			CalcRTask readDataTask = createTask("Read " + entity.getName() + " data");
			
			// 1. update output variables to null
			UpdateQuery<Record> upd = new Psql().updateQuery(table);//.set(null, null).
			for (String field : outputVariables) {
				//skip primary key
				if( !field.equals(primaryKey) ){
					//TODO what if there are other types of field to update other than double?
					
					Field<Double> f = (Field<Double>) table.field(field);
					upd.addValue( f, DSL.val(null, Double.class) );
				}
			}
			readDataTask.addScript( r().dbSendQuery(connection, upd) );
			
			// 2. append select data
			SelectQuery<Record> select = new Psql().selectQuery();
			select.addFrom(view);
			select.addSelect(view.getIdField());
			for (String var : inputVariables) {
				select.addSelect( view.field(var) );
			}
			readDataTask.addScript( r().setValue(dataFrame, r().dbGetQuery(connection, select)) );
			addTask(readDataTask);
			
			
			// ======= add all calculation step tasks
			addTasks(calculationStepTasks);
			
			
			// ======= write results to db
			CalcRTask writeResultsTask = createTask("Write " + entity.getName() + " results");
			
			
			// 4. convert primary key field to string otherwise integers are stored as real. (R doesnt manage int type. all numbers are real)
			RVariable pkeyVar = r().variable(dataFrame, primaryKey);
			writeResultsTask.addScript( r().setValue( pkeyVar, r().asCharacter(pkeyVar) ) );
			
			
			// 5. keep results (only pkey and output variables) 
			RVariable results = r().variable( entity.getName()+"_results" );
			RVector cols = r().c( outputVariables.toArray(new String[]{}) ).addValue( primaryKey );
			
			writeResultsTask.addScript( r().setValue(results, dataFrame.filterColumns(cols)) );
			
			
			// remove Inf numbers from results
			RScript removeInf = r().rScript("is.na(" + results + "[ , unlist(lapply(" + results + ", is.numeric))] ) <-  " + results + "[ , unlist(lapply(" + results + ", is.numeric))] == Inf");
			writeResultsTask.addScript( removeInf );
			
			// 6. remove results table
			ResultTable resultTable = getInputSchema().getResultTable( entity );
			writeResultsTask.addScript( r().dbRemoveTable(connection, resultTable.getName()) );
			
			// 7. write results to db
			writeResultsTask.addScript( r().dbWriteTable(connection, resultTable.getName(), results) );

			// 8. for each output field, update table with results joining with results table
			// convert id datatype from varchar to bigint first
			AlterColumnStep alterPkey = new Psql()
				.alterTable(resultTable)
				.alterColumn( resultTable.getIdField() )
				.type(SQLDataType.BIGINT)
				.using(resultTable.getIdField().getName() + "::bigint");
			
			writeResultsTask.addScript( r().dbSendQuery(connection, alterPkey) );
			
			SelectQuery<Record> selectResults = new Psql().selectQuery();	
			selectResults.addFrom(resultTable);
			selectResults.addSelect( resultTable.getIdField() );
			for (String var : outputVariables) {
				selectResults.addSelect( resultTable.field(var) );
			}
			Table<?> cursor = selectResults.asTable("r");
	
			UpdateQuery<Record> updateResults = new Psql().updateQuery(table);
			for (String var : outputVariables) {
				updateResults.addValue( (Field<BigDecimal>)table.field(var), (Field<BigDecimal>)cursor.field(var));
			}
			
			UpdateWithStep update = new Psql()
				.updateWith(cursor, updateResults, table.getIdField().eq( (Field<Long>) cursor.field(resultTable.getIdField().getName()) ) );
			
			writeResultsTask.addScript( r().dbSendQuery(connection, update) );
			
			addTask( writeResultsTask );
		}
		
		// 9. close connection
		CalcRTask closeConnection = createTask("Close database connection");
		closeConnection.addScript( r().dbDisconnect(connection) );
		addTask(closeConnection);
	}

	private CalcRTask createTask(String name) {
		CalcRTask task = new CalcRTask(rEnvironment , name);
		((AutowireCapableBeanFactory)beanFactory).autowireBean(task);
		return task;
	}

	@Override
	protected long countTotalItems() {
		return this.calculationSteps.size();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for ( Task task : tasks() ) {
			sb.append( task.toString() );
		}
		return sb.toString();
	}
	
}

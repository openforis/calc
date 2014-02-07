/**
 * 
 */
package org.openforis.calc.engine;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.SchemaImpl;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.post.CreateAggregateTablesTask;
import org.openforis.calc.chain.post.PublishRolapSchemaTask;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.psql.AlterTableStep.AlterColumnStep;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.calc.r.DbConnect;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RLogger;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.RVector;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mino Togna
 * 
 */
public class CalcJob extends Job {

	// save results in a temporary results table 
	private boolean tempResults;
	
	
	@JsonIgnore
	// private List<CalculationStep> calculationSteps;
//	private Map<Integer, List<CalculationStep>> calculationSteps;
	private CalcJobEntityGroup group;
	
	@Autowired
	@JsonIgnore
	private BeanFactory beanFactory;

	/**
	 * RLogger used by calcRTask.execute
	 */
	private RLogger rLogger;

	@Value("${calc.jdbc.host}")
	private String host;
	
	@Value("${calc.jdbc.db}")
	private String database;
	
	@Value("${calc.jdbc.username}")
	private String user;
	
	@Value("${calc.jdbc.password}")
	private String password;
	
	@Value("${calc.jdbc.port}")
	private int port;

	private boolean aggregates;

	protected RVariable connection;


	private REnvironment rEnvironment;

	protected CalcJob(Workspace workspace, DataSource dataSource, BeanFactory beanFactory) {
		this(workspace, dataSource, beanFactory, false);
	}
	
	/**
	 * @param workspace
	 * @param dataSource
	 */
	protected CalcJob(Workspace workspace, DataSource dataSource, BeanFactory beanFactory, boolean aggregates) {
		super(workspace, dataSource);
		setSchemas(new Schemas(workspace));

		this.rLogger = new RLogger();
		this.beanFactory = beanFactory;
		this.group = new CalcJobEntityGroup(this);
		
		this.tempResults = false;
		this.aggregates = aggregates;
	}

	// calculation steps are grouped by entity for performance reason
	public void addCalculationStep(CalculationStep step) {
		this.group.addCalculationStep(step);
	}

	public void addCalculationStep(List<CalculationStep> steps) {
		for (CalculationStep calculationStep : steps) {
			addCalculationStep(calculationStep);
		}
	}

	@Override
	public void init() {
		this.rEnvironment = newREnvironment();

		initTasks();

		super.init();
	}

	@SuppressWarnings("unchecked")
	protected void initTasks() {
		
		// init task
		openConnection();
		
		
		// init entity groups
		this.group.init(connection);
		
		// execute the calculation steps grouped by entity
		for (Integer entityId : this.group.entityIds() ) {
//		for (Integer entityId : this.calculationSteps.keySet()) {
			Entity entity = getWorkspace().getEntityById(entityId);
			EntityDataView view = getSchemas().getInputSchema().getDataView(entity);
			InputTable table = getSchemas().getInputSchema().getDataTable(entity);
			Field<?> primaryKeyField = view.getPrimaryKey().getFields().get(0);
			String primaryKey = primaryKeyField.getName();
			RVariable dataFrame = r().variable(entity.getName());
			
			// ===== read data task
			CalcRTask readDataTask = createTask("Read " + entity.getName() + " data");

			// 1. update output variables to null
			ResultTable finalResultTable = getSchemas().getInputSchema().getResultTable(entity);
			UpdateQuery<Record> upd = new Psql().updateQuery(finalResultTable);// .set(null,
			for (String field : group.getOutputVariables(entityId) ) {
				// skip primary key
				if (!field.equals(primaryKey)) {
					// TODO what if there are other types of field to update
					// other than double?

					Field<Double> f = (Field<Double>) table.field(field);
					upd.addValue(f, DSL.val(null, Double.class));
				}
			}
			readDataTask.addScript(r().dbSendQuery(connection, upd));

			// 2. append select data
			SelectQuery<Record> select = new Psql().selectQuery();
			select.addFrom(view);
			select.addSelect(view.getIdField());
			for (String var : group.getInputVariables(entityId) ) {
				select.addSelect(view.field(var));
			}
			readDataTask.addScript(r().setValue(dataFrame, r().dbGetQuery(connection, select)));
			addTask(readDataTask);
			 
			// append plot_area script
			RScript plotArea = group.getPlotAreaScript(entityId);
			if( plotArea != null ) {
				RVariable results = r().variable( "results" );
				SetValue setValue = r().setValue(results, r().rTry(plotArea) );
				readDataTask.addScript( setValue );
			}
			
			
			// ======= add all calculation step tasks
			addTasks( group.getCalculationStepTasks(entityId) );

			// ======= write results to db
			CalcRTask writeResultsTask = createTask("Write " + entity.getName() + " results");

			// 4. convert primary key field to string otherwise integers are
			// stored as real. (R doesnt manage int type. all numbers are real)
			RVariable pkeyVar = r().variable(dataFrame, primaryKey);
			writeResultsTask.addScript(r().setValue(pkeyVar, r().asCharacter(pkeyVar)));

			// 5. keep results (only pkey and output variables)
			RVariable results = r().variable( entity.getName() + "_results" );
			RVector cols = r().c(group.getAllOutputVariables(entityId).toArray(new String[] {})).addValue(primaryKey);

			writeResultsTask.addScript(r().setValue(results, dataFrame.filterColumns(cols)));

			// remove Inf numbers from results
			RScript removeInf = r().rScript("is.na(" + results + "[ , unlist(lapply(" + results + ", is.numeric))] ) <-  " + results + "[ , unlist(lapply(" + results + ", is.numeric))] == Inf");
			writeResultsTask.addScript(removeInf);

			// 6. remove results table
			ResultTable resultTable = getInputSchema().getResultTable(entity, tempResults);
//			writeResultsTask.addScript(r().dbRemoveTable(connection, resultTable.getName()));
			writeResultsTask.addScript( r().dbSendQuery(connection, new Psql().dropTableIfExists(resultTable).cascade()) );
			// 7. write results to db
			writeResultsTask.addScript(r().dbWriteTable(connection, resultTable.getName(), results));

			// 8. for each output field, update table with results joining with
			// results table
			// convert id datatype from varchar to bigint first
			AlterColumnStep alterPkey = 
					new Psql()
						.alterTable(resultTable)
						.alterColumn(resultTable.getIdField())
						.type(SQLDataType.BIGINT)
						.using(resultTable.getIdField().getName() + "::bigint");

			writeResultsTask.addScript(r().dbSendQuery(connection, alterPkey));

//			SelectQuery<Record> selectResults = new Psql().selectQuery();
//			selectResults.addFrom(resultTable);
//			selectResults.addSelect(resultTable.getIdField());
//			Collection<String> outputVariables = group.getOutputVariables(entityId);
//			for (String var : outputVariables ) {
//				selectResults.addSelect(resultTable.field(var));
//			}
//			Table<?> cursor = selectResults.asTable("r");
//
//			UpdateQuery<Record> updateResults = new Psql().updateQuery(table);
//			for (String var : outputVariables) {
//				updateResults.addValue((Field<BigDecimal>) table.field(var), (Field<BigDecimal>) cursor.field(var));
//			}
//
//			UpdateWithStep update = new Psql().updateWith(cursor, updateResults, table.getIdField().eq((Field<Long>) cursor.field(resultTable.getIdField().getName())));
//
//			writeResultsTask.addScript(r().dbSendQuery(connection, update));

			// update tree view if it's not temporary results 
			if( tempResults ) {
				
				// if results table doesn't exists yet, it needs to create it anyway 
//				DbWriteTable writeResults = r().dbWriteTable(connection, entity.getResultsTable(), results);
//				If writeResultsIfItDoesntExist = r().rIf( r().not(r().dbExistsTable(connection, entity.getResultsTable())), writeResults );
//				writeResultsTask.addScript(	writeResultsIfItDoesntExist );

				// update results with temporary results
				
				
				SelectQuery<Record> selectResults = new Psql().selectQuery();
				selectResults.addFrom(resultTable);
				selectResults.addSelect(resultTable.getIdField());
				Collection<String> outputVariables = group.getOutputVariables(entityId);
				for (String var : outputVariables ) {
					selectResults.addSelect(resultTable.field(var));
				}
				Table<?> cursor = selectResults.asTable("r");
	
				UpdateQuery<Record> updateResults = new Psql().updateQuery(finalResultTable);
				for (String var : outputVariables) {
					updateResults.addValue((Field<BigDecimal>) finalResultTable.field(var), (Field<BigDecimal>) cursor.field(var));
				}
	
				UpdateWithStep update = new Psql().updateWith(cursor, updateResults, finalResultTable.getIdField().eq((Field<Long>) cursor.field(resultTable.getIdField().getName())));
	
				writeResultsTask.addScript(r().dbSendQuery(connection, update));
				
				
//			} else{
//				getWorkspace().
				
			}
			
			// recreate view
			DropViewStep dropViewIfExists = new Psql().dropViewIfExists(view);
			writeResultsTask.addScript(r().dbSendQuery( connection, dropViewIfExists ));
			
			Select<?> selectView = view.getSelect(true);
			AsStep createView = new Psql().createView(view).as(selectView);
			writeResultsTask.addScript(r().dbSendQuery( connection, createView ));
	//			}
			
			addTask(writeResultsTask);

			
			
		}
		
		// 9. close connection
		closeConnection();
		
		
		if( aggregates ) {
//			CreateFactTablesTask task = new CreateFactTablesTask();
//			((AutowireCapableBeanFactory) beanFactory).autowireBean(task);
//			addTask( task );
				
				CreateAggregateTablesTask aggTask = new CreateAggregateTablesTask();
				((AutowireCapableBeanFactory) beanFactory).autowireBean(aggTask);
				addTask( aggTask );
				
				PublishRolapSchemaTask publishRolapSchemaTask = new PublishRolapSchemaTask();
				((AutowireCapableBeanFactory) beanFactory).autowireBean(publishRolapSchemaTask);
				addTask( publishRolapSchemaTask );
			}
	}

	protected void closeConnection() {
		CalcRTask closeConnection = createTask("Close database connection");
		closeConnection.addScript( r().dbDisconnect(connection) );
		addTask(closeConnection);
	}

	protected void openConnection() {
		CalcRTask initTask = createTask("Open database connection");

		// init libraries
//		initTask.addScript(r().library("lmfor"));
		initTask.addScript(r().library("RPostgreSQL"));
		// common functions //org/openforis/calc/r/functions.R
		initTask.addScript(RScript.getCalcRScript());
		// create driver
		RVariable driver = r().variable("driver");
		initTask.addScript(r().setValue(driver, r().dbDriver("PostgreSQL")));

		connection = r().variable("connection");
		DbConnect dbConnect = r().dbConnect(driver, host, database, user, password, port);
		initTask.addScript(r().setValue(connection, dbConnect));

		// set search path to current and public schemas
		initTask.addScript(r().dbSendQuery(connection, new Psql().setDefaultSchemaSearchPath(getInputSchema(), new SchemaImpl("public"))));
		
		addTask(initTask);
	}

	protected CalcRTask createTask(String name) {
		CalcRTask task = new CalcRTask(rEnvironment, name);
		((AutowireCapableBeanFactory) beanFactory).autowireBean(task);
		return task;
	}

	@Override
	protected long countTotalItems() {
//		return this.calculationSteps.size();
		return tasks().size();
	}

	public void setAggregates(boolean aggregates) {
		this.aggregates = aggregates;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Task task : tasks()) {
			sb.append(task.toString());
		}
		return sb.toString();
	}

	@JsonInclude
	public RLogger getRLogger() {
		return this.rLogger;
	}

	REnvironment getrEnvironment() {
		return rEnvironment;
	}

	public boolean isTempResults() {
		return tempResults;
	}

	public void setTempResults(boolean tempResults) {
		this.tempResults = tempResults;
	}

	
}

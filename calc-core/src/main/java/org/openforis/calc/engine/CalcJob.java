/**
 * 
 */
package org.openforis.calc.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.chain.post.CreateAggregateTablesTask;
import org.openforis.calc.chain.post.PublishRolapSchemaTask;
import org.openforis.calc.metadata.Aoi;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Entity.Visitor;
import org.openforis.calc.metadata.ErrorSettings;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.persistence.jooq.ParameterMapConverter;
import org.openforis.calc.psql.AlterTableStep.AlterColumnStep;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.calc.r.DbConnect;
import org.openforis.calc.r.DbSendQuery;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RLogger;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.RVector;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.EntityDataViewDao;
import org.openforis.calc.schema.ErrorTable;
import org.openforis.calc.schema.FactTable;
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
	
	@Autowired
	private ProcessingChainManager processingChainService;
	
	@JsonIgnore
	private CalcJobEntityGroup group;
	
	@Autowired
	@JsonIgnore
	private BeanFactory beanFactory;

	@Autowired
	@JsonIgnore
	private EntityDataViewDao entityDataViewDao;
	
	@Autowired
	@JsonIgnore
	Psql psql;
	
	private ProcessingChain processingChain;
	
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

	public void addCalculationSteps(List<CalculationStep> steps) {
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
		CalcRTask intTask = openConnection();
		
		// init entity groups
		this.group.init(connection);
		
		List<Task> readDataTasks = new ArrayList<Task>();
		List<Task> executeTasks = new ArrayList<Task>();
		List<Task> writeResultsTasks = new ArrayList<Task>();
		
		// execute the calculation steps grouped by entity
		Workspace workspace = getWorkspace();
		for ( Integer entityId : this.group.entityIds() ){

			Entity entity = workspace.getEntityById(entityId);
			EntityDataView view = getSchemas().getDataSchema().getDataView(entity);
			Field<?> primaryKeyField = view.getPrimaryKey().getFields().get(0);
			String primaryKey = primaryKeyField.getName();
			RVariable dataFrame = r().variable(entity.getName());
			
			// ===== read data task
			CalcRTask readDataTask = createTask("Read " + entity.getName() + " data");

			// 1. update output variables to null
			ResultTable finalResultTable = getSchemas().getDataSchema().getResultTable(entity);
			UpdateQuery<Record> upd = new Psql().updateQuery(finalResultTable);// .set(null,
			for (String field : group.getOutputVariables(entityId) ) {
				// skip primary key
				if (!field.equals(primaryKey)) {
					// TODO what if there are other types of field to update
					// other than double?

					Field<Double> f = (Field<Double>) finalResultTable.field(field);
					upd.addValue(f, DSL.val(null, Double.class));
				}
			}
			readDataTask.addScript( r().dbSendQuery(connection, upd) );
			
			Set<String> selectFields = new HashSet<String>();
			// 2. append select data
			SelectQuery<Record> select = new Psql().selectQuery();
			select.addFrom(view);
			select.addSelect(view.getIdField());
			// add ids up to the root
			select.addSelect( view.getAncestorIdFields() );
			for (String var : group.getInputVariables(entityId) ) {
				if( !selectFields.contains(var)){
					select.addSelect(view.field(var));
					selectFields.add(var);
				}
			}
			
			// add plot area variables to select as well
			RScript plotArea = group.getPlotAreaScript(entityId);
			if( plotArea != null ){
				Set<String> variables = plotArea.getVariables();
				for ( String var : variables ) {
					if( !selectFields.contains(var)){
						select.addSelect( view.field(var) );	
						selectFields.add(var);
					}
				}
			}
			
			readDataTask.addScript(r().setValue(dataFrame, r().dbGetQuery(connection, select)));
			// append plot_area script
			if( plotArea != null ) {
				RVariable results = r().variable( "results" );
				SetValue setValue = r().setValue(results, r().rTry(plotArea) );
				readDataTask.addScript( setValue );
			}
			readDataTasks.add(readDataTask);
//			addTask(readDataTask);
			
			
			// ======= add all calculation step tasks
//			addTasks( group.getCalculationStepTasks(entityId) );
			executeTasks.addAll( group.getCalculationStepTasks(entityId) );

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
			}
			
			// recreate view
			DropViewStep dropViewIfExists = new Psql().dropViewIfExists(view);
			writeResultsTask.addScript(r().dbSendQuery( connection, dropViewIfExists ));
			
			Select<?> selectView = view.getSelect(true);
			AsStep createView = new Psql().createView(view).as(selectView);
			writeResultsTask.addScript(r().dbSendQuery( connection, createView ));
			
//			addTask(writeResultsTask);
			writeResultsTasks.add( writeResultsTask );
		}
		
		addTasks( readDataTasks );
		addTasks( executeTasks );
		addTasks( writeResultsTasks );
		
		
		if( aggregates ) {
				
			CreateAggregateTablesTask aggTask = new CreateAggregateTablesTask();
			( (AutowireCapableBeanFactory) beanFactory ).autowireBean( aggTask );
			addTask( aggTask );
			
			// create error tables first
			boolean errorScriptAdded = false;
			CalcRTask createErrorTableTask = createTask( "Create error tables" );
			List<FactTable> factTables = getSchemas().getDataSchema().getFactTables();
			ErrorSettings errorSettings = workspace.getErrorSettings();

			for ( FactTable factTable : factTables ){
				List<ErrorTable> errorTables = factTable.getErrorTables();
				for ( ErrorTable errorTable : errorTables ){
					
					if( !errorScriptAdded ){
						RScript errorScript = r().rScript( errorSettings.getScript() );
						intTask.addScript( errorScript );
//						intTask.addScript( RScript.getErrorEstimationScript() );
						errorScriptAdded = true;
						
						addTask( createErrorTableTask );
					}
					// drop error table
					DbSendQuery dropErrorTable = r().dbSendQuery( connection, psql.dropTableIfExists(errorTable) );
					createErrorTableTask.addScript(dropErrorTable);
					// create error table
					DbSendQuery createErrorTable = r().dbSendQuery( connection , psql.createTable(errorTable, errorTable.fields()) );
					createErrorTableTask.addScript( createErrorTable );
				}
			}
			
			// add error calculation tasks 
			Set< QuantitativeVariable > outputVariables = this.group.uniqueOutputQuantitativeVariables();
			for ( QuantitativeVariable outputVariable : outputVariables ){
				long variableId = outputVariable.getId().longValue();
				if( errorSettings.hasErrorSettings(variableId) ){
					
					Collection<? extends Number> aois 					= errorSettings.getAois( variableId );
					Collection<? extends Number> categoricalVariables 	= errorSettings.getCategoricalVariables( variableId );
					
					for ( Number aoiId : aois ){
						for ( Number categoricalVariableId : categoricalVariables ){
							CategoricalVariable<?> categoricalVariable = (CategoricalVariable<?>) workspace.getVariableById( categoricalVariableId.intValue() );
							Aoi aoi = workspace.getAoiHierarchies().get(0).getAoiById( aoiId.intValue() );
							
							
							
							// aoi might be null during collect import phase
//							if( aoi!= null && categoricalVariable !=null ){
							FactTable factTable = getSchemas().getDataSchema().getFactTable( outputVariable.getEntity() );
							ErrorTable errorTable = factTable.getErrorTable( outputVariable, aoi, categoricalVariable );
//							}
							CalculateErrorTask calculateErrorTask = new CalculateErrorTask( this , errorTable , connection , aoi );
							addTask( calculateErrorTask );

						}
					}
					
				}
			}
			
			
			
			PublishRolapSchemaTask publishRolapSchemaTask = new PublishRolapSchemaTask();
			((AutowireCapableBeanFactory) beanFactory).autowireBean(publishRolapSchemaTask);
			addTask( publishRolapSchemaTask );
		}
		
		// 9. close connection
		closeConnection();
		
		// 10 - hidden to users: re-creates views for sampling unit and its descendant
		if( workspace.hasSamplingDesign() ){
			Entity samplingUnit = workspace.getSamplingUnit();
			samplingUnit.traverse( new Visitor() {
				@Override
				public void visit(Entity entity) {
					entityDataViewDao.createOrUpdateView(entity);
					
				}
			});
		}
		
	}

	protected void closeConnection() {
		CalcRTask closeConnection = createTask("Close database connection");
		closeConnection.addScript( r().dbDisconnect(connection) );
		addTask(closeConnection);
	}

	protected CalcRTask openConnection() {
		CalcRTask initTask = createTask("Open database connection");

		// init libraries
//		initTask.addScript(r().library("lmfor"));
//		initTask.addScript(r().library("RPostgreSQL"));
		// common functions //org/openforis/calc/r/functions.R
		initTask.addScript(RScript.getCalcCommonScript());
		// create driver
		RVariable driver = r().variable("driver");
		initTask.addScript(r().setValue(driver, r().dbDriver("PostgreSQL")));

		connection = r().variable("connection");
		DbConnect dbConnect = r().dbConnect(driver, host, database, user, password, port);
		initTask.addScript(r().setValue(connection, dbConnect));

		// set search path to current and public schemas
		initTask.addScript(r().dbSendQuery(connection, new Psql().setDefaultSchemaSearchPath(getInputSchema(), new SchemaImpl("public"))));
		
		addTask(initTask);
		
		return initTask;
	}

	protected CalcRTask createTask(String name) {
		CalcRTask task = new CalcRTask(rEnvironment, name);
		autowire(task);
		return task;
	}

	private void autowire( Object obj ){
		((AutowireCapableBeanFactory) beanFactory).autowireBean(obj);
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
			if( task instanceof CalcRTask ){
				sb.append(task.toString());
			}
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
	
	@JsonInclude
	public ProcessingChain getProcessingChain() {
		return processingChain;
	}

	void setProcessingChain(ProcessingChain processingChain) {
		this.processingChain = processingChain;
		List<CalculationStep> steps = this.processingChain.getCalculationSteps();
		for (CalculationStep calculationStep : steps) {
			if( calculationStep.getActive() ){
				this.addCalculationStep(calculationStep);
			}
		}
//		this.addCalculationSteps( steps );
	}
	
	/**
	 * Set also  the status to the processing chain if associated to the job
	 */
	@Override
	void setStatus(Status status) {
		super.setStatus(status);
		if( this.processingChain != null ){
			processingChainService.updateProcessingChainStatus( processingChain, status );
		}
	}

	public static void main(String[] args) {
		String str = "{\"functions\":[\"a\",\"b\",\"c\"]}";
		ParameterMapConverter c = new ParameterMapConverter();
		ParameterMap map = c.from( str );
		System.out.println( map.toString() );
		Collection<? extends Object> array = map.getArray("functions");
		for (Object object : array) {
			System.out.println( object );
		}
//		ParameterHashMap m = new ParameterHashMap();
		
		
	}
}

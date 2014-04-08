package org.openforis.calc.engine;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.Insert;
import org.jooq.Record;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.AoiDao;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.StratumDao;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.metadata.VariableAggregateDao;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.persistence.jooq.tables.daos.SamplingDesignDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.EntityDataViewDao;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.DataSchemaDao;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
//@Scope( WebApplicationContext.SCOPE_SESSION )
public class WorkspaceService {

	@Autowired
	private WorkspaceDao workspaceDao;

	@Autowired
	private MetadataManager workspaceManager;
	
	@Autowired
	private EntityDao entityDao;

	@Autowired
	private EntityDataViewDao entityDataViewDao;
	
	@Autowired
	private VariableDao variableDao;

//	@Autowired
//	private VariableAggregateDao variableAggregateDao;

	@Autowired
	private DataSchemaDao inputSchemaDao;

	@Autowired
	private ProcessingChainService processingChainService;

	@Autowired
	private SamplingDesignDao samplingDesignDao;

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private AoiDao aoiDao;

	@Autowired
	private StratumDao stratumDao;
	
//	private Workspace activeWorkspace;

	public WorkspaceService() {
	}

	public Workspace get( int workspaceId ) {
		return workspaceManager.fetchWorkspaceById( workspaceId );
	}

//	@Transactional
//	public Workspace fetchByName(String name) {
//		return workspaceDao.fetchByName(name);
//	}

	public Workspace fetchByCollectSurveyUri( String uri ) {
		return workspaceManager.fetchWorkspaceByCollectSurveyUri( uri );
	}

	public Workspace save(Workspace workspace) {
		return workspaceManager.saveWorkspace( workspace );
	}

	public List<Workspace> loadAll() {
		return workspaceManager.findAllWorkspaces();
	}

	/**
	 * It returns the active workspace
	 * 
	 * @return
	 */
	public Workspace getActiveWorkspace() {
		Workspace workspace = workspaceManager.fetchActiveWorkspace();
			
		if ( workspace != null ) {
			List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
			// set root aoi to each aoiHierarchy linked to the workspace
//			for (AoiHierarchy aoiHierarchy : aoiHierarchies) {
//				aoiDao.assignRootAoi(aoiHierarchy);
//			}
		}
		return workspace;
	}

	public Workspace createAndActivate(String name, String uri, String schema) {
		workspaceManager.deactivateAll();

		Workspace ws = new Workspace();
		ws.setActive(true);
		ws.setCollectSurveyUri(uri);
		ws.setInputSchema(schema);
		ws.setName(name);
		ws.setCaption(name);
		ws = workspaceManager.saveWorkspace(ws);

		processingChainService.createDefaultProcessingChain(ws);

		setActiveWorkspace(ws);
		
		return ws;
	}

	public QuantitativeVariable addOutputVariable(Entity entity, String name) {
		
		// get result table
//		InputSchema schema = new Schemas( entity.getWorkspace() ).getInputSchema();
//		ResultTable originalResultTable = schema.getResultTable(entity);
		QuantitativeVariable variable = createQuantitativeVariable(name);

		entity.addVariable(variable);
//		ResultTable resultTable = schema.getResultTable(entity);
		
		
//		addVariableColumn(variable);
		
		// add column to results table and update entity view
//		if( originalResultTable == null) { 
//			try {
////				CreateTableWithFieldsStep createTable = new Psql(dataSource)
////					.createTable(resultTable, resultTable.fields());
////					createTable.execute();
//					
//					resetResultTable(resultTable, schema.getDataTable(entity) );
//					
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} else {
//			new Psql(dataSource)
//				.alterTable(resultTable)
//				.addColumn( resultTable.getQuantityField(variable) )
//				.execute();
//		}
//		
		
		variableDao.saveWorkspace(variable);
		updateEntityView(variable);

		return variable;
	}

	public void updateResultTable( CalculationStep calculationStep ){
		QuantitativeVariable variable = (QuantitativeVariable) calculationStep.getOutputVariable();
		Entity entity = variable.getEntity();
		
		// get result table
		DataSchema schema = new Schemas( entity.getWorkspace() ).getDataSchema();
		ResultTable originalResultTable = schema.getResultTable(entity);
//			QuantitativeVariable variable = createQuantitativeVariable(name);

//			entity.addVariable(variable);
		ResultTable resultTable = schema.getResultTable(entity);
			
			
//			addVariableColumn(variable);
			
			// add column to results table and update entity view
			if( originalResultTable == null) { 
				try {
//					CreateTableWithFieldsStep createTable = new Psql(dataSource)
//						.createTable(resultTable, resultTable.fields());
//						createTable.execute();
						
						resetResultTable(resultTable, schema.getDataTable(entity) );
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				new Psql(dataSource)
					.alterTable(resultTable)
					.addColumn( resultTable.getQuantityField(variable) )
					.execute();
			}
			
			
//			variableDao.save(variable);
			updateEntityView(variable);

//			return variable;
	}
	
	private QuantitativeVariable createQuantitativeVariable(String name) {
		QuantitativeVariable variable = new QuantitativeVariable();
		variable.setName(name);
		variable.setInputValueColumn(name);
		variable.setOutputValueColumn(name);
		variable.setScale(Scale.RATIO);
		return variable;
	}

//	private void addVariableColumn(QuantitativeVariable variable){
//		inputSchemaDao.addUserDefinedVariableColumn(variable);
//	}
//
//	private void dropVariableColumn(QuantitativeVariable variable){
//		inputSchemaDao.dropUserDefinedVariableColumn(variable);
//	}
	
	private void updateEntityView(QuantitativeVariable variable) {
		Entity entity = getEntity(variable);
		entityDataViewDao.createOrUpdateView(entity);
	}

	private Entity getEntity(QuantitativeVariable variable) {
		Entity entity = variable.getEntity();
		if (entity == null) {
			QuantitativeVariable sourceVariable = variable.getSourceVariable();
			if(sourceVariable == null){
				throw new IllegalStateException("Unable to find entity to update for variable " + variable.getName());
			}
			entity = sourceVariable.getEntity();
		}
		return entity;
	}

	public void addUserDefinedVariableColumns(Workspace ws) {
		for (Variable<?> v : ws.getUserDefinedVariables()) {
			if (v instanceof QuantitativeVariable) {
				inputSchemaDao.addUserDefinedVariableColumn((QuantitativeVariable) v);
			}
		}
	}

//	/**
//	 * Creates a column in the input schema entity table for each variable per ha defined
//	 */
//	@Transactional
//	public void addVariablePerHaColumns(Workspace ws) {
//		for (Entity entity : ws.getEntities()) {
//			for (QuantitativeVariable v : entity.getQuantitativeVariables()) {
//				QuantitativeVariable variablePerHa = v.getVariablePerHa();
//				if ( variablePerHa != null ) {
//					inputSchemaDao.addUserDefinedVariableColumn(variablePerHa);
//				}
//			}
//		}
//	}
	
	public void activate( Workspace ws ) {
		workspaceManager.activate( ws );
		setActiveWorkspace( ws );
	}
	
	
	public void resetDataViews(Workspace ws) {
		for (Entity entity : ws.getEntities()) {
			entityDataViewDao.createOrUpdateView(entity);
		}
	}
	
	/**
	 * Remove all results table and recreates them empty
	 */
	public void resetResults(Workspace ws) {
		
//		resetDataViews( ws );
		
		DataSchema schema = new Schemas(ws).getDataSchema();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			
			// if not sampling unit
			if( ! entity.isSamplingUnit() ) {
				// first drop the view
				EntityDataView view = schema.getDataView(entity);
				entityDataViewDao.drop(view);
				
				ResultTable resultsTable = schema.getResultTable(entity);
				InputTable dataTable = schema.getDataTable(entity);
				// then it creates the result table
				resetResultTable(resultsTable, dataTable);
				
				// last it recreates the views
				entityDataViewDao.create(view);
			}
			
		}
		
	}

	protected void resetResultTable(ResultTable resultsTable, InputTable dataTable) {
		if( resultsTable != null ) {
			new Psql(dataSource)
				.dropTableIfExists(resultsTable)
				.execute();
			
			new Psql(dataSource)
				.createTable(resultsTable, resultsTable.fields())
				.execute();
			
			Insert<Record> insert = new Psql(dataSource)
				.insertInto(resultsTable, resultsTable.getIdField() )
				.select( new Psql().select(dataTable.getIdField()).from(dataTable) );
			
			insert.execute();
		}
	}

	@Transactional
	public Workspace setActiveWorkspaceSamplingUnit(Integer entityId) {
		Workspace workspace = getActiveWorkspace();
		SamplingDesign samplingDesign = workspace.getSamplingDesign();
		if (samplingDesign == null) {
			samplingDesign = new SamplingDesign();
		} 
		
		Entity samplingUnit = workspace.getEntityById(entityId);
		samplingDesign.setSamplingUnit(samplingUnit);
		samplingDesignDao.save(samplingDesign);
		workspace.setSamplingDesign(samplingDesign);
		
		workspace = save(workspace);
		return workspace;
	}

	@Transactional
	public QuantitativeVariable createVariableAggregate(Workspace workspace, int entityId, int variableId, String agg) {
		Entity entity = workspace.getEntityById(entityId);
		QuantitativeVariable variable = entity.getQtyVariableById(variableId);
		
		int variableReturnId = 0;
		//the variable id passed is a variable-per-ha linked to another variable
		if(variable == null) {
			variable = entity.getQtyVariablePerHaById(variableId);
			variableReturnId = variable.getSourceVariable().getId();
		} else {
			variableReturnId = variable.getId();
		}
		
		if (!variable.hasAggregate(agg)) {
			if (VariableAggregate.AGGREGATE_TYPE.isValid(agg)) {
				VariableAggregate varAgg = new VariableAggregate();
				varAgg.setVariable(variable);
				varAgg.setAggregateType(agg);
				varAgg.setAggregateFormula("");
				variableAggregateDao.saveWorkspace(varAgg);
			} else {
				throw new IllegalArgumentException("Invalild aggregate type: " + agg);
			}
		}
		variable = (QuantitativeVariable) variableDao.fetchWorkspaceById(variableReturnId);
		
//		updateEntityView(variable);
		
		return variable;
	}
	
	@Transactional
	public QuantitativeVariable deleteVariableAggregate(Workspace workspace, int entityId, int variableId, String agg) {
		Entity entity = workspace.getEntityById(entityId);
		QuantitativeVariable variable = entity.getQtyVariableById(variableId);
		
		int variableReturnId = 0;
		//the variable id passed is a variable-per-ha linked to another variable
		if(variable == null) {
			variable = entity.getQtyVariablePerHaById(variableId);
			variableReturnId = variable.getSourceVariable().getId();
		} else {
			variableReturnId = variable.getId();
		}
		
		VariableAggregate aggregate = variable.getAggregate(agg);
		if (aggregate != null) {
			variable.deleteAggregate(agg);
			variableDao.saveWorkspace(variable);
			variableAggregateDao.delete(aggregate.getId());
		}
		
		variable = (QuantitativeVariable) variableDao.fetchWorkspaceById(variableReturnId);
		
//		updateEntityView(variable);
		
		return variable;
	}

	@Transactional
	public QuantitativeVariable addVariablePerHa(QuantitativeVariable variable) {
		QuantitativeVariable variablePerHa = variable.getVariablePerHa();

		if (variablePerHa == null) {
			
			String name = variable.getName() + "_per_ha";
			variablePerHa = createQuantitativeVariable(name);
			variablePerHa.setSourceVariable(variable);
			
			variable.setVariablePerHa(variablePerHa);
			
			variableDao.saveWorkspace(variablePerHa);			
			variable = (QuantitativeVariable) variableDao.saveWorkspace(variable);
			
//			addVariableColumn(variablePerHa);
//			updateEntityView(variablePerHa);
		}

		return variable;
	}

	@Transactional
	public QuantitativeVariable deleteVariablePerHa(QuantitativeVariable variable) {
		return deleteVariablePerHa(variable, true);
	}

	@Transactional
	public QuantitativeVariable deleteVariablePerHa(QuantitativeVariable variable, boolean updateEntityView) {
		QuantitativeVariable variablePerHa = variable.getVariablePerHa();

		if (variablePerHa != null) {
//			dropVariableColumn(variablePerHa);
			
			variable.setVariablePerHa(null);
			variableDao.delete(variablePerHa.getId());
			variable = (QuantitativeVariable) variableDao.saveWorkspace(variable);
			
//			if ( updateEntityView ) {
//				updateEntityView(variable);
//			}
		}

		return variable;
	}
	
	@Transactional
	public void deleteOutputVariable(QuantitativeVariable variable, boolean updateEntityView) {
		QuantitativeVariable variablePerHa = variable.getVariablePerHa();
		if ( variablePerHa != null ) {
			deleteVariablePerHa(variable, false);
		}
//		dropVariableColumn(variable);
		
		Entity entity = getEntity(variable);
		
		DataSchema schema = new Schemas(entity.getWorkspace()).getDataSchema();
		ResultTable originalResultsTable = schema.getResultTable(entity);
		EntityDataView view = schema.getDataView(entity);

		// drop entity data view
		entityDataViewDao.drop(view);
		
		// drop column from results table
//		new Psql(dataSource)
//			.alterTable(originalResultsTable)
//			.dropColumn( originalResultsTable.getQuantityField(variable) )
//			.execute();
		
		// delete variable
		variableDao.delete(variable.getId());
		entity.removeVariable(variable);
		
		// drop result table, if there are no more output variables
		ResultTable newResultTable = schema.getResultTable(entity);
		if ( newResultTable == null ) {
			new Psql(dataSource)
				.dropTableIfExists(originalResultsTable)
				.execute();
		}
		
//		if ( updateEntityView ) {
			updateEntityView(variable);
//		}
	}
	
	/**
	 * Set plot area script for the given entity and returns it
	 * @param entity
	 * @param plotAreaScript
	 * @return
	 */
	@Transactional
	public Entity setEntityPlotAreaScript(Entity entity, String plotAreaScript) {
		entity.setPlotAreaScript(plotAreaScript);
		Entity updEntity = entityDao.saveWorkspace(entity);
		return updEntity;
	}

	@Transactional
	public void importStrata(Workspace workspace, String filepath) throws IOException {
		stratumDao.deleteAll(workspace);
		
		CsvReader csvReader = new CsvReader(filepath);
		csvReader.readHeaders();
		
//		FlatRecord record = csvReader.nextRecord();
		for(FlatRecord record = csvReader.nextRecord() ; record != null ; record = csvReader.nextRecord() ) {
			Integer no = record.getValue(0, Integer.class);
			String caption = record.getValue(1, String.class);
			
			stratumDao.add(workspace, no, caption);
		}
		
	}

	public void resetResultTable(Entity entity){
		DataSchema schema = new Schemas( entity.getWorkspace() ).getDataSchema();
		EntityDataView dataView = schema.getDataView(entity);
		ResultTable resultsTable = schema.getResultTable(entity);
		InputTable dataTable = schema.getDataTable(entity);
		
		if( resultsTable != null ){
			//drop data view first
			entityDataViewDao.drop(dataView);
			
			new Psql(dataSource)
				.dropTableIfExists(resultsTable)
				.execute();
			
			new Psql(dataSource)
				.createTable(resultsTable, resultsTable.fields())
				.execute();
			
			Insert<Record> insert = new Psql(dataSource)
					.insertInto(resultsTable, resultsTable.getIdField() )
					.select(
							new Psql()
							.select( dataTable.getIdField() )
							.from(dataTable)
					);
			insert.execute();
			
			entityDataViewDao.createOrUpdateView(entity);
		}
	}
	
	private void setActiveWorkspace(Workspace activeWorkspace) {
//		this.activeWorkspace = activeWorkspace;
	}
}

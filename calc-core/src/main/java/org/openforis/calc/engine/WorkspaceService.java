package org.openforis.calc.engine;

import java.io.IOException;
import java.util.List;

import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.metadata.AoiDao;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MetadataManager;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.StratumDao;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.metadata.CategoryLevel.CategoryLevelValue;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.WorkspaceTable;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.persistence.jooq.tables.daos.SamplingDesignDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.DataSchemaDao;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataViewDao;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;
import org.openforis.calc.schema.TableDao;
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
	private MetadataManager metadataManager;
	@Autowired
	private EntityDao entityDao;
	@Autowired
	private EntityDataViewDao entityDataViewDao;
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private ProcessingChainManager processingChainService;
	@Autowired
	private DataSchemaDao inputSchemaDao;
	@Autowired
	private SamplingDesignDao samplingDesignDao;
	@Autowired
	private AoiDao aoiDao;
	@Autowired
	private StratumDao stratumDao;
	@Autowired
	private TableDao tableDao;
	@Autowired
	private CalculationStepDao calculationStepDao;
	@Autowired
	private CategoryManager categoryManager;
	
	@Autowired
	private Psql psql;

	public WorkspaceService() {
	}

	public Workspace get( int workspaceId ) {
		return metadataManager.fetchWorkspaceById( workspaceId );
	}

	public Workspace fetchByCollectSurveyUri( String uri ) {
		return metadataManager.fetchWorkspaceByCollectSurveyUri( uri );
	}

	public Workspace save(Workspace workspace) {
		return metadataManager.saveWorkspace( workspace );
	}

	public List<Workspace> loadAll() {
		return metadataManager.findAllWorkspaces();
	}

	public List<Workspace> loadAllInfos(){
		return metadataManager.loadAllWorksaceInfos();
	}
	
	public void deleteInputCategories(Workspace workspace) {
		categoryManager.deleteInputCategories(workspace);
	}
	/**
	 * It returns the active workspace
	 * 
	 * @return
	 */
	public Workspace getActiveWorkspace() {
		Workspace workspace = metadataManager.fetchActiveWorkspace();
		return workspace;
	}

	/**
	 * It returns the active workspace
	 * 
	 * @return
	 */
	public Integer getActiveWorkspaceId() {
		WorkspaceTable T = Tables.WORKSPACE;
		Integer id = psql.select(T.ID).from(T).where( T.ACTIVE.isTrue() ).fetchOne().getValue(T.ID);
		return id;
	}
	
	public Workspace createAndActivate(String name, String uri, String schema) {
		metadataManager.deactivateAll();

		Workspace ws = new Workspace();
		ws.setActive( true );
		ws.setCollectSurveyUri( uri );
		ws.setInputSchema( schema );
		ws.setName( name );
		ws.setCaption( name );
		ws = metadataManager.saveWorkspace( ws );

		processingChainService.createDefaultProcessingChain( ws );
//		setActiveWorkspace(ws);
		return ws;
	}

	@Transactional
	public QuantitativeVariable addQuantityVariable( Entity entity, String name ) {
		QuantitativeVariable variable = metadataManager.createQuantitativeVariable(name);
		entity.addVariable(variable);
		saveVariable( variable );

		return variable;
	}

	@Transactional
	public MultiwayVariable addMultiwayVariable( Entity entity, String name ) {
		MultiwayVariable variable = metadataManager.createMultiwayVariable( name );
		entity.addVariable(variable);
		saveVariable(variable);
		
		return variable;
	}
	@Transactional
	public void saveVariable(Variable<?> variable) {
		variableDao.save( variable );
	}
	

//	public void addUserDefinedVariableColumns(Workspace ws) {
//		for (Variable<?> v : ws.getUserDefinedVariables()) {
//			if (v instanceof QuantitativeVariable) {
//				inputSchemaDao.addUserDefinedVariableColumn((QuantitativeVariable) v);
//			}
//		}
//	}

	public void activate( Workspace ws ) {
		metadataManager.activate( ws );
	}
	
	
	@Transactional
	public void resetWorkspace( Workspace ws ) {
		resetSamplingUnitWeight( ws );
		resetResults( ws );
	}
	
	@Transactional
	private void resetSamplingUnitWeight( Workspace ws ) {
		Entity samplingUnit = ws.getSamplingUnit();
		if( samplingUnit != null ) {
			
			DataSchema schema = new Schemas(ws).getDataSchema();
			DataTable dataTable = schema.getDataTable( samplingUnit );
			psql
				.alterTable(dataTable)
				.dropColumnIfExists( dataTable.getWeightField() , true )
				.execute();
		
			psql
				.alterTable(dataTable)
				.addColumn( dataTable.getWeightField() )
				.execute();
		}
	}
	/**
	 * Remove all results table and recreates them empty
	 */
	@Transactional
	public void resetResults( Workspace ws ){
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			resetResults( entity );
		}
	}
	
	/**
	 * Reset result table for given entity
	 * @param entity
	 */
	@Transactional
	private void resetResults( Entity entity ) {
		DataSchema schema = new Schemas( entity.getWorkspace() ).getDataSchema();
		ResultTable resultsTable = schema.getResultTable(entity);
		DataTable dataTable = schema.getDataTable(entity);
		
		entityDataViewDao.drop( entity );
		
		if( resultsTable != null ) {
			//drop data view first
			psql
				.dropTableIfExists( resultsTable )
				.execute();
			
			psql
				.createTable( resultsTable, resultsTable.fields() )
				.execute();
			
			Insert<Record> insert = psql
					.insertInto( resultsTable, resultsTable.getIdField() )
					.select(
							new Psql()
							.select( dataTable.getIdField() )
							.from( dataTable )
					);
			insert.execute();
			
		}
		entityDataViewDao.createOrUpdateView( entity );
	}
	
	/**
	 * Reset result column for the given variable
	 * 
	 * @param entity
	 * @param variable
	 */
	@Transactional
	public void resetResult( Variable<?> variable ) {
		Entity entity = variable.getEntity();
		DataSchema schema = new Schemas( entity.getWorkspace() ).getDataSchema();
		ResultTable resultTable = schema.getResultTable( entity );
		
		boolean exists = resultTable != null && tableDao.exists( resultTable.getSchema().getName() , resultTable.getName() );
		if( exists ) {
			entityDataViewDao.drop( entity );
			
			Field<?> field = resultTable.field( variable.getOutputValueColumn() );
			psql
				.alterTable( resultTable )
				.dropColumnIfExists( field )
				.execute();
			psql
				.alterTable( resultTable )
				.addColumn( field )
				.execute();
			
			if( variable instanceof MultiwayVariable ){
				
				Field<?> idField = resultTable.field( variable.getInputCategoryIdColumn() );
				psql
					.alterTable( resultTable )
					.dropColumnIfExists( idField )
					.execute();
				psql
					.alterTable( resultTable )
					.addColumn( idField )
					.execute();
			}
			
			entityDataViewDao.createOrUpdateView( entity );
		} else if( !exists ) {
			resetResults( entity );
		}
	}
	

	
	@Transactional
	public void deleteVariable(Variable<?> variable , boolean createView) {
		Entity entity = variable.getEntity();
		metadataManager.deleteVariable(variable);
		
		if( variable.isUserDefined() ){

			DataSchema schema = new Schemas( entity.getWorkspace() ).getDataSchema();
			ResultTable resultTable = schema.getResultTable( entity );
			
			boolean exists = resultTable != null && tableDao.exists( resultTable.getSchema().getName() , resultTable.getName() );
			if( exists ) {
				entityDataViewDao.drop( entity );
				
				Field<?> field = resultTable.field( variable.getOutputValueColumn() );
				psql
					.alterTable( resultTable )
					.dropColumnIfExists( field )
					.execute();
				
				if( variable instanceof MultiwayVariable ){
					
					Field<?> idField = resultTable.field( variable.getInputCategoryIdColumn() );
					psql
						.alterTable( resultTable )
						.dropColumnIfExists( idField )
						.execute();
				}
				
				if( createView ){
					entityDataViewDao.createOrUpdateView( entity );
				}
				
			} else if( !exists ) {
				resetResults( entity );
			}
		
		}
	}
	
	/**
	 * Set plot area script for the given entity and returns it
	 * @param entity
	 * @param plotAreaScript
	 * @return
	 */
	@Transactional
	public Entity setEntityPlotAreaScript(Entity entity, String plotAreaScript) {
		entity.setPlotAreaScript( plotAreaScript );
		entityDao.update( entity );
		return entity;
	}

	@Transactional
	public void importStrata(Workspace workspace, String filepath) throws IOException {
		stratumDao.deleteAll(workspace);
		
		@SuppressWarnings( "resource" )
		CsvReader csvReader = new CsvReader(filepath);
		csvReader.readHeaders();
		
//		FlatRecord record = csvReader.nextRecord();
		for(FlatRecord record = csvReader.nextRecord() ; record != null ; record = csvReader.nextRecord() ) {
			Integer no = record.getValue(0, Integer.class);
			String caption = record.getValue(1, String.class);
			
			stratumDao.insert(workspace, no, caption);
		}
		
	}
	
	/**
	 * Add a new category to the workspace
	 * @param workspace
	 * @param category
	 */
	public void addCategory( Workspace workspace , Category category , List<CategoryLevelValue> values ) {
		categoryManager.createCategory( workspace, category , values );
	}

//	private void setActiveWorkspace(Workspace activeWorkspace) {
////		this.activeWorkspace = activeWorkspace;
//	}
}

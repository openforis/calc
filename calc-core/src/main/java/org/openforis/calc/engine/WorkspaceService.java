package org.openforis.calc.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jooq.Insert;
import org.jooq.Record;
import org.openforis.calc.metadata.AoiDao;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EntityDao;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.StratumDao;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.metadata.VariableAggregateDao;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.psql.CreateTableWithFieldsStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.EntityDataViewDao;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.InputSchemaDao;
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
public class WorkspaceService {

	@Autowired
	private WorkspaceDao workspaceDao;

	@Autowired
	private EntityDao entityDao;

	@Autowired
	private EntityDataViewDao entityDataViewDao;
	
	@Autowired
	private VariableDao variableDao;

	@Autowired
	private VariableAggregateDao variableAggregateDao;

	@Autowired
	private InputSchemaDao inputSchemaDao;

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
	
	private Map<Integer, SimpleLock> locks;

	public WorkspaceService() {
		this.locks = new HashMap<Integer, SimpleLock>();
	}

	@Transactional
	public Workspace get(int workspaceId) {
		return workspaceDao.find(workspaceId);
	}

	@Transactional
	public Workspace fetchByName(String name) {
		return workspaceDao.fetchByName(name);
	}

	@Transactional
	public Workspace fetchCollectSurveyUri(String uri) {
		return workspaceDao.fetchByCollectSurveyUri(uri);
	}

	@Transactional
	public Workspace save(Workspace workspace) {
		return workspaceDao.save(workspace);
	}

	@Transactional
	public List<Workspace> loadAll() {
		return workspaceDao.loadAll();
	}

	/**
	 * It returns the active workspace
	 * 
	 * @return
	 */
	public Workspace getActiveWorkspace() {
		Workspace workspace = workspaceDao.fetchActive();
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		// set root aoi to each aoiHierarchy linked to the workspace
		for (AoiHierarchy aoiHierarchy : aoiHierarchies) {
			aoiDao.assignRootAoi(aoiHierarchy);
		}
		return workspace;
	}

	synchronized public SimpleLock lock(int workspaceId) throws WorkspaceLockedException {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			lock = new SimpleLock();
			locks.put(workspaceId, lock);
		}
		if (!lock.tryLock()) {
			throw new WorkspaceLockedException();
		}
		return lock;
	}

	synchronized public boolean isLocked(int workspaceId) {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			return false;
		} else {
			return lock.isLocked();
		}
	}

	public Workspace createAndActivate(String name, String uri, String schema) {
		workspaceDao.deactivateAll();

		Workspace ws = new Workspace();
		ws.setActive(true);
		ws.setCollectSurveyUri(uri);
		ws.setInputSchema(schema);
		ws.setName(name);
		ws.setCaption(name);
		workspaceDao.save(ws);

		processingChainService.createDefaultProcessingChain(ws);

		return ws;
	}

	public QuantitativeVariable addOutputVariable(Entity entity, String name) {
		
		// get result table
		InputSchema schema = new Schemas( entity.getWorkspace() ).getInputSchema();
		ResultTable originalTable = schema.getResultTable(entity);
		
		QuantitativeVariable variable = createQuantitativeVariable(name);

		entity.addVariable(variable);

		variableDao.save(variable);

//		addVariableColumn(variable);
		
		// add column to results table and update entity view
		ResultTable resultTable = schema.getResultTable(entity);
		if( originalTable == null) { 
			CreateTableWithFieldsStep createTable = new Psql(dataSource)
				.createTable(resultTable, resultTable.fields());
				createTable.execute();
		} else {
			new Psql(dataSource)
				.alterTable(resultTable)
				.addColumn( resultTable.getQuantityField(variable) )
				.execute();
		}
		
		updateEntityView(variable);

		return variable;
	}

	private QuantitativeVariable createQuantitativeVariable(String name) {
		QuantitativeVariable variable = new QuantitativeVariable();
		variable.setName(name);
		variable.setInputValueColumn(name);
		variable.setOutputValueColumn(name);
		variable.setScale(Scale.RATIO);
		return variable;
	}

	private void addVariableColumn(QuantitativeVariable variable){
		inputSchemaDao.addUserDefinedVariableColumn(variable);
	}

	private void dropVariableColumn(QuantitativeVariable variable){
		inputSchemaDao.dropUserDefinedVariableColumn(variable);
	}
	
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
	
	public void activate(Workspace ws) {
		workspaceDao.deactivateAll();
		ws.setActive(true);
		workspaceDao.save(ws);
	}

	public void createViews(Workspace ws) {
		for (Entity entity : ws.getEntities()) {
			entityDataViewDao.createOrUpdateView(entity);
		}
	}
	
	/**
	 * Remove all results table and recreates them empty
	 */
	public void resetResults(Workspace ws) {
		InputSchema schema = new Schemas(ws).getInputSchema();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			ResultTable resultsTable = schema.getResultTable(entity);
			InputTable dataTable = schema.getDataTable(entity);
			
			if( resultsTable != null ) {
				new Psql()
				.dropTableIfExists(resultsTable)
				.execute();
				
				new Psql()
					.createTable(resultsTable, resultsTable.fields())
					.execute();
				
				Insert<Record> insert = new Psql()
					.insertInto(resultsTable, resultsTable.getIdField() )
					.select( new Psql().select(dataTable.getIdField()).from(dataTable) );
				
				insert.execute();
			}	
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
		
		workspace = workspaceDao.save(workspace);
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
				variableAggregateDao.save(varAgg);
			} else {
				throw new IllegalArgumentException("Invalild aggregate type: " + agg);
			}
		}
		variable = (QuantitativeVariable) variableDao.find(variableReturnId);
		
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
			variableDao.save(variable);
			variableAggregateDao.delete(aggregate.getId());
		}
		
		variable = (QuantitativeVariable) variableDao.find(variableReturnId);
		
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
			
			variableDao.save(variablePerHa);			
			variable = (QuantitativeVariable) variableDao.save(variable);
			
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
			variable = (QuantitativeVariable) variableDao.save(variable);
			
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
		
		variableDao.delete(variable.getId());

		entity.removeVariable(variable);
		
		// drop column from results table
		InputSchema schema = new Schemas(entity.getWorkspace()).getInputSchema();
		ResultTable table = schema.getResultTable(entity);
		
		new Psql(dataSource)
			.alterTable(table)
			.dropColumn( table.getQuantityField(variable) )
			.execute();
		
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
		Entity updEntity = entityDao.save(entity);
		return updEntity;
	}

	@Transactional
	public void importStrata(Workspace workspace, String filepath) throws IOException {
		stratumDao.deleteAll(workspace);
		
		CsvReader csvReader = new CsvReader(filepath);
		csvReader.readHeaders();
		
//		FlatRecord record = csvReader.nextRecord();
		for(FlatRecord record = csvReader.nextRecord() ; record != null ; record = csvReader.nextRecord()) {
			Integer no = record.getValue(0, Integer.class);
			String caption = record.getValue(1, String.class);
			
			stratumDao.add(workspace, no, caption);
			
			
			record = csvReader.nextRecord();
		}
		
	}

}

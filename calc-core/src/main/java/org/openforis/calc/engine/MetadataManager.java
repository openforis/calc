/**
 * 
 */
package org.openforis.calc.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.persistence.jooq.tables.daos.ProcessingChainDao;
import org.openforis.calc.persistence.jooq.tables.daos.SamplingDesignDao;
import org.openforis.calc.persistence.jooq.tables.daos.StratumDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class responsible for reading/writing metadata objects linked to the workspace
 * 
 * @author Mino Togna
 * @author S. Ricci
 *
 */
@Repository
public class MetadataManager {

	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Autowired
	private StratumDao stratumDao;
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired
	private CalculationStepDao calculationStepDao; 
	
	@Autowired
	private EntityDao entityDao; 
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private SamplingDesignDao samplingDesignDao;
	
	@Autowired
	private AoiManager aoiManager;
	
	@Autowired
	private Psql psql;
	
	/*
	 * ============================
	 * 	Load workspace methods
	 * ============================
	 */
	@Transactional
	public Workspace fetchWorkspaceById( int workspaceId ) {
		Workspace workspace = workspaceDao.findById( workspaceId );
		
		loadMetadata( workspace );
		
		return workspace;
	}
	
	@Transactional
	public Workspace fetchWorkspaceByCollectSurveyUri( String uri ) {
		List<Workspace> list = workspaceDao.fetchByCollectSurveyUri( uri );
		if( CollectionUtils.isNotEmpty(list) ) {
			if( list.size() > 1 ){
				throw new IllegalStateException( "Found more than one workspace with survey uri: " + uri );
			}
			Workspace workspace = list.get(0);
			loadMetadata( workspace );
			return workspace;
		}
		return null;
	}
	
	@Transactional
	public List<Workspace> findAllWorkspaces() {
		List<Workspace> list = workspaceDao.findAll();
		for (Workspace workspace : list) {
			loadMetadata( workspace );
		}
		return list;
	}
	
	@Transactional
	public Workspace fetchActiveWorkspace() {
		Workspace workspace = workspaceDao.fetchOne( Tables.WORKSPACE.ACTIVE, true );
		if( workspace != null ) {
			loadMetadata( workspace );
		}
		return workspace;
	}
	
	/* 
	 * ===============================
	 *  Load metadata methods
	 * ===============================
	 */
	@Transactional
	private void loadMetadata( Workspace workspace ) {
		aoiManager.loadByWorkspace( workspace );
		
		loadEntities( workspace );
		loadStrata( workspace );
		loadProcessingChains( workspace );
		loadSamplingDesign( workspace );
		
		initEntityHierarchy( workspace );
	}
	
	private void initEntityHierarchy(Workspace workspace) {
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			Integer parentEntityId = entity.getParentEntityId();
			if( parentEntityId != null ){
				Entity parent = workspace.getEntityById( parentEntityId );
				parent.addChild( entity );
			}
		}
	}

	private void loadSamplingDesign(Workspace workspace) {
		SamplingDesign samplingDesign = samplingDesignDao.fetchOne( Tables.SAMPLING_DESIGN.WORKSPACE_ID , workspace.getId() );
		workspace.setSamplingDesign( samplingDesign );
	}

	private void loadEntities( Workspace workspace ) {
		List<Entity> entities = entityDao.fetchByWorkspaceId( workspace.getId() );
		Collections.sort( entities, new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				return o1.getSortOrder().compareTo( o2.getSortOrder() );
			}
		});
		for (Entity entity : entities) {
			workspace.addEntity( entity );
		}
		variableDao.loadByWorkspace( workspace );
		
	}

	private void loadStrata(Workspace workspace) {
		List<Stratum> strata = stratumDao.fetchByWorkspaceId( workspace.getId() );
		for (Stratum stratum : strata) {
			workspace.addStratum(stratum);
		}
	}
	
	private void loadProcessingChains(Workspace workspace) {
		List<ProcessingChain> chains = processingChainDao.fetchByWorkspaceId( workspace.getId() );
		for (ProcessingChain chain : chains) {
			workspace.addProcessingChain(chain);
			loadSteps( chain );
		}
	}
	
	private void loadSteps(ProcessingChain chain) {
		List<CalculationStep> steps = calculationStepDao.fetchByChainId( chain.getId() );
		Collections.sort( steps, new Comparator<CalculationStep>() {
			@Override
			public int compare(CalculationStep o1, CalculationStep o2) {
				return o1.getStepNo().compareTo( o2.getStepNo() );
			}
		});
		for (CalculationStep step : steps) {
			chain.addCalculationStep( step );
			Workspace workspace = chain.getWorkspace();
			step.setOutputVariable( workspace.getVariableById(step.getOutputVariableId()) );
		}
		
	}
	/*
	 * ============================
	 *  Save workspace methods
	 * ============================
	 */
	/**
	 * Saves the workspace and its metadata
	 * 
	 * @param workspace
	 * @return
	 */
	@Transactional
	public Workspace saveWorkspace( Workspace workspace ) {
		if( workspaceDao.exists(workspace) ) {
			workspaceDao.update( workspace );
		} else {
			workspaceDao.insert( workspace );
		}
		
		saveMetadata( workspace );
		
		return fetchWorkspaceByCollectSurveyUri( workspace.getCollectSurveyUri() );
	}
	/* 
	 * ===============================
	 *  Save metadata methods
	 * ===============================
	 */
	@Transactional
	private void saveMetadata( Workspace workspace ) {
		
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			Integer id = entity.getId();
			if( id == null ){
				//TODO check if entity ids has been set to all variables
				entityDao.insert( entity );
			} else {
				entityDao.update( entity );
			}
			
			variableDao.save( entity.getVariables() );
			
		}
		
		
//		saveStrata( workspace );
//		saveProcessingChains( workspace );
	}
	
//	@Transactional
//	public void saveStrata(Workspace workspace) {
//		for ( Stratum stratum : workspace.getStrata() ) {
//			if( stratumDao.exists( stratum ) ) {
//				stratumDao.update( stratum );
//			} else {
//				stratumDao.insert( stratum );
//			}
//		}
//		
//	}
	
	/* 
	 * ===============================
	 *  Delete metadata methods
	 * ===============================
	 */
	@Transactional
	public void deleteVariables(Collection<Variable<?>> variables) {
		for (Variable<?> variable : variables) {
			Entity entity = variable.getEntity();
			entity.removeVariable(variable);
			variableDao.delete(variable);
		}
	}
	
	@Transactional
	public void deleteEntities(Collection<Entity> entities) {
		for ( Entity entity : entities ) {
			Workspace ws = entity.getWorkspace();
			List<Variable<?>> variables = entity.getVariables();
			deleteVariables(variables);
			
			entityDao.delete(entity);
			
			ws.removeEntity(entity);
		}
	}
	
	/*
	 * ===========================
	 * 	Workspace utility methods
	 * ===========================
	 */
	public void deactivateAll() {
		List<Workspace> list = findAllWorkspaces();
		for (Workspace ws : list) {
			ws.setActive( false );
			workspaceDao.update( ws );
		}
	}

	public void activate(Workspace ws) {
		deactivateAll();
		ws.setActive( true );
		workspaceDao.update( ws );
	}

	public void insertEntity(Workspace ws, Entity entity) {
		ws.addEntity(entity);
		entityDao.insert(entity);
		Integer entityId = entity.getId();
		
		List<Variable<?>> variables = entity.getVariables();
		for (Variable<?> variable : variables) {
			variable.setEntityId(entityId);
			variableDao.insert(variable);
		}
	}

	public void insertVariable(Entity entity, Variable<?> variable) {
		entity.addVariable(variable);
		variableDao.insert(variable);
	}
	
	/*
	 * ========================
	 * Workspace update methods
	 * ========================
	 */
	@Transactional
	public Workspace applyChangesToWorkspace(Workspace ws, List<Entity> newEntities) {
		//remove deleted entities
		Collection<Entity> entitiesToBeRemoved = new HashSet<Entity>();
		for (Entity oldEntity : ws.getEntities()) {
			Entity newEntity = getEntityByOriginalId(newEntities, oldEntity.getOriginalId());
			if ( newEntity == null ) {
				entitiesToBeRemoved.add(oldEntity);
			}
		}
		deleteEntities(entitiesToBeRemoved);
		
		//apply changes to existing entities
		for (Entity oldEntity : ws.getEntities()) {
			Entity newEntity = getEntityByOriginalId(newEntities, oldEntity.getOriginalId());
			if ( newEntity != null ) {
				applyChangesToEntity(oldEntity, newEntity);
			}
		}
		
		//add new entities
		for (Entity newEntity : newEntities) {
			Entity oldEntity = ws.getEntityByOriginalId(newEntity.getOriginalId());
			if ( oldEntity == null ) {
				replaceParentEntityWithPersistedOne(newEntity);
				
				insertEntity(ws, newEntity);
			}
		}

		//TODO children entity ids not updated after save...check this
//		Workspace reloaded = workspaceDao.find(ws.getId());
//		ws.setEntities(reloaded.getEntities());
		return ws;
	}

	protected void replaceParentEntityWithPersistedOne(Entity newEntity) {
		Workspace ws = newEntity.getWorkspace();
		Entity newParent = newEntity.getParent();
		if ( newParent != null ) {
			Integer parentOriginalId = newParent.getOriginalId();
			if ( parentOriginalId != null ) {
				Entity persistedParent = ws.getEntityByOriginalId(parentOriginalId);
				if ( persistedParent != null ) {
					newEntity.setParent(persistedParent);
				}
			}
		}
	}

	private void applyChangesToEntity(Entity oldEntity, Entity newEntity) {
		//update entity attributes
		oldEntity.setCaption(newEntity.getCaption());
		oldEntity.setDataTable(newEntity.getDataTable());
		oldEntity.setDescription(newEntity.getDescription());
		oldEntity.setIdColumn(newEntity.getIdColumn());
		oldEntity.setLocationColumn(newEntity.getLocationColumn());
		oldEntity.setName(newEntity.getName());
		oldEntity.setParentIdColumn(newEntity.getParentIdColumn());
//		oldEntity.setSamplingUnit(newEntity.isSamplingUnit());
		oldEntity.setSrsColumn(newEntity.getSrsColumn());
		oldEntity.setUnitOfAnalysis(newEntity.getUnitOfAnalysis());
		oldEntity.setXColumn(newEntity.getXColumn());
		oldEntity.setYColumn(newEntity.getYColumn());
		
		//remove deleted variables
		Collection<Variable<?>> variablesToBeRemoved = new HashSet<Variable<?>>();
		for (Variable<?> oldVariable : oldEntity.getVariables()) {
			Integer oldVariableOrigId = oldVariable.getOriginalId();
			if ( oldVariableOrigId != null ) {
				Variable<?> newVariable = newEntity.getVariableByOriginalId(oldVariableOrigId);
				if ( newVariable == null ) {
					variablesToBeRemoved.add(oldVariable);
				}
			}
		}
		deleteVariables(variablesToBeRemoved);
		
		//apply changes to existing variables
		for (Variable<?> oldVariable : oldEntity.getVariables()) {
			Integer oldVariableOrigId = oldVariable.getOriginalId();
			if ( oldVariableOrigId != null ) {
				Variable<?> newVariable = newEntity.getVariableByOriginalId(oldVariableOrigId);
				applyChangesToVariable(oldVariable, newVariable);
				variableDao.update(oldVariable);
			}
		}
		
		//add new variables
		for (Variable<?> newVariable : newEntity.getVariables()) {
			Variable<?> oldVariable = oldEntity.getVariableByOriginalId(newVariable.getOriginalId());
			if ( oldVariable == null ) {
				insertVariable(oldEntity, newVariable);
			}
		}
	}
	
	private void applyChangesToVariable(Variable<?> oldVariable, Variable<?> newVariable) {
		oldVariable.setCaption(newVariable.getCaption());
		setDefaultValue(oldVariable, newVariable);
		oldVariable.setDescription(newVariable.getDescription());
		oldVariable.setDimensionTable(newVariable.getDimensionTable());
		//TODO update variable name and inputValueColumn: handle taxon attribute variables (2 variables per each attribute definition)
//		oldVariable.setInputValueColumn(newVariable.getInputValueColumn());
		//oldVariable.setName(newVariable.getName());
		oldVariable.setOutputValueColumn(newVariable.getOutputValueColumn());
		if ( newVariable instanceof MultiwayVariable ) {
			MultiwayVariable v1 = (MultiwayVariable) oldVariable;
			MultiwayVariable v2 = (MultiwayVariable) newVariable;
			v1.setInputCategoryIdColumn(v2.getInputCategoryIdColumn());
			v1.setDimensionTable(v2.getDimensionTable());
			v1.setDimensionTableIdColumn(v2.getDimensionTableIdColumn());
			v1.setDimensionTableCodeColumn(v2.getDimensionTableCodeColumn());
			v1.setDimensionTableCaptionColumn(v2.getDimensionTableCaptionColumn());
		}
	}
	
	private Entity getEntityByOriginalId(List<Entity> entities, int originalId) {
		for (Entity entity : entities) {
			if ( originalId == entity.getOriginalId().intValue() )  {
				return entity;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Object> void setDefaultValue(Variable<?> oldVariable,
			Variable<?> newVariable) {
		((Variable<T>) oldVariable).setDefaultValue((T) newVariable.getDefaultValueTemp());
	}

}

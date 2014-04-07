/**
 * 
 */
package org.openforis.calc.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.VariableTable;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.persistence.jooq.tables.daos.ProcessingChainDao;
import org.openforis.calc.persistence.jooq.tables.daos.SamplingDesignDao;
import org.openforis.calc.persistence.jooq.tables.daos.StratumDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.persistence.jooq.tables.pojos.VariableBase;
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
	public void deleteVariablesByOriginalId( Workspace workspace , Integer... ids ) {
		Set<Integer> entityIds = new HashSet<Integer>();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			entityIds.add( entity.getId() );
			
			entity.getVariableByOriginalId( id );
		}
		
		VariableTable T = Tables.VARIABLE;
		psql
			.delete( T )
			.where( T.ORIGINAL_ID.in(ids) )
			.and( T.ENTITY_ID.in(entityIds) )
			.execute();
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

}

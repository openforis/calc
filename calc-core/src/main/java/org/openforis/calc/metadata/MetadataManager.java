/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class responsible for reading/writing metadata objects linked to the workspace
 * TODO : rename to WorkspaceManager? 
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
	private SamplingDesignManager samplingDesignManager;
	
	@Autowired
	private AoiManager aoiManager;
	
	@Autowired
	private CategoryManager categoryManager;
	
	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private VariableManager variableManager;
	
	@Autowired
	private EquationManager equationManager;
	
	@Autowired
	private ErrorSettingsManager errorSettingsManager; 
	
	@Autowired
	private Psql psql;

	@Autowired
	private ProcessingChainManager processingChainManager;
	
	@Autowired
	private WorkspaceSettingsManager workspaceSettingsManager;
	
	@Autowired
	private AuxiliaryTableManager auxiliaryTableManager;
	
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
	
//	@Transactional
//	public Workspace fetchWorkspaceByCollectSurveyUri( String uri ) {
//		List<Workspace> list = workspaceDao.fetchByCollectSurveyUri( uri );
//		if( CollectionUtils.isNotEmpty(list) ) {
//			if( list.size() > 1 ){
//				throw new IllegalStateException( "Found more than one workspace with survey uri: " + uri );
//			}
//			Workspace workspace = list.get(0);
//			loadMetadata( workspace );
//			return workspace;
//		}
//		return null;
//	}
	
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
		// the order matters here. 
		aoiManager.loadByWorkspace( workspace );
		categoryManager.load( workspace );
		entityManager.loadEntities( workspace );
		samplingDesignManager.loadStrata( workspace );
		samplingDesignManager.loadStrataAois(workspace);
		processingChainManager.loadChains( workspace );
		samplingDesignManager.loadSampligDesign( workspace );
		equationManager.loadEquationLists( workspace );
		errorSettingsManager.load( workspace );
		workspaceSettingsManager.load( workspace );
		auxiliaryTableManager.loadAll( workspace );
		
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
	public Workspace saveWorkspace( Workspace workspace ){
		if( workspaceDao.exists(workspace) ) {
			workspaceDao.update( workspace );
		} else {
			Long nextval = psql.nextval( Sequences.WORKSPACE_ID_SEQ );
			workspace.setId( nextval.intValue() );
			workspaceDao.insert( workspace );
		}
		
		categoryManager.save( workspace );
		entityManager.persistEntities( workspace );
		errorSettingsManager.save( workspace );
		workspaceSettingsManager.save( workspace );
		
//		return fetchWorkspaceByCollectSurveyUri( workspace.getCollectSurveyUri() );
		return fetchWorkspaceById( workspace.getId() );
	}
	
	/* 
	 * ===============================
	 *  Delete metadata methods
	 * ===============================
	 */
	@Transactional
	public void deleteVariables(Collection<Variable<?>> variables) {
		for (Variable<?> variable : variables) {
			deleteVariable( variable );
		}
	}
	
	@Transactional
	public void deleteVariable(Variable<?> variable) {
		variableManager.delete( variable );
	}
	
	@Transactional
	public void deleteEntities( Collection<Entity> entities ) {
		for ( Entity entity : entities ) {
			entityManager.delete( entity );
		}
	}
	
	/*
	 * ===========================
	 * 	Workspace utility methods
	 * ===========================
	 */
	/**
	 * Deactivates all workspaces
	 */
	@Transactional
	public void deactivateAll() {
		psql
			.update( Tables.WORKSPACE )
			.set( Tables.WORKSPACE.ACTIVE , false )
			.execute();
	}
	
	/**
	 * Activates a workspace. It deactivates all first
	 * @param ws
	 */
	@Transactional
	public void activate( Workspace ws ) {
		deactivateAll();
		
		ws.setActive( true );
		workspaceDao.update( ws );
	}
	
	/**
	 * Returns all workspaces ordered by name without loading their metadata
	 * @return
	 */
	public List<Workspace> loadAllWorksaceInfos() {
		List<Workspace> list = workspaceDao.findAll();
		Collections.sort( list, new Comparator<Workspace>() {
			@Override
			public int compare(Workspace o1, Workspace o2) {
				return o1.getName().compareTo( o2.getName() );
			}
		});
		return list;
	}

	/**
	 * Create an instance of a new quantitative variable with default values
	 * 
	 * @param name
	 * @return
	 */
	public QuantitativeVariable createQuantitativeVariable( String name ) {
		QuantitativeVariable variable = new QuantitativeVariable();
		
		variable.setName( name );
		variable.setInputValueColumn( name );
		variable.setOutputValueColumn( name );
		variable.setScale( Scale.RATIO );
		variable.setOverride( true );
		variable.setDegenerateDimension( false );
		variable.setDisaggregate( false );
		
		return variable;
	}
	
	/**
	 * Create an instance of a new categorical variable with default values
	 * 
	 * @param name
	 * @param levelId 
	 * @return
	 */
	public MultiwayVariable createMultiwayVariable( String name ) {
//		QuantitativeVariable variable = new QuantitativeVariable();
		MultiwayVariable variable = new MultiwayVariable();
		variable.setName( name );
		variable.setInputValueColumn( name );
		variable.setInputCategoryIdColumn( name +"_id" );
		variable.setOutputValueColumn( name );
		variable.setScale( Scale.NOMINAL );
		variable.setOverride( true );
		variable.setDegenerateDimension( false );
		variable.setDisaggregate( true );
		
		return variable;
	}

	public void deleteInputVariables(Workspace ws) {
		// remove input variables
		for (Entity entity : ws.getEntities()) {
			entity.deleteInputVariables();
		}
	}

}

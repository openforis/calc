/**
 * 
 */
package org.openforis.calc.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EquationManager;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.CategoryDao;
import org.openforis.calc.persistence.jooq.tables.daos.CategoryHierarchyDao;
import org.openforis.calc.persistence.jooq.tables.daos.CategoryLevelDao;
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
	private SamplingDesignDao samplingDesignDao;	
	@Autowired
	private StratumDao stratumDao;
	@Autowired
	private AoiManager aoiManager;
	
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private CategoryHierarchyDao categoryHierarchyDao;
	@Autowired
	private CategoryLevelDao categoryLevelDao;
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	@Autowired
	private CalculationStepDao calculationStepDao; 
	
	@Autowired
	private EntityDao entityDao; 
	@Autowired
	private VariableDao variableDao;
	
	@Autowired
	private EquationManager equationManager;
	
	
	@Autowired
	private Psql psql;

	@Autowired
	private ProcessingChainManager processingChainManager;
	
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
		// the order matters here. 
		loadAois(workspace);
		loadCategories( workspace );
		loadEntities( workspace );
		loadStrata( workspace );
		loadProcessingChains( workspace );
		loadSamplingDesign( workspace );
		loadEquations( workspace );
		
		initEntityHierarchy( workspace );
	}

	private void loadAois(Workspace workspace) {
		aoiManager.loadByWorkspace( workspace );
	}
	
	private void loadCategories( Workspace workspace ){
		List<Category> categories = categoryDao.fetchByWorkspaceId( workspace.getId().longValue() );
		for ( Category category : categories ){
			workspace.addCategory( category );
			
			List<CategoryHierarchy> hierarchies = this.categoryHierarchyDao.fetchByCategoryId( category.getId().longValue() );
			for ( CategoryHierarchy categoryHierarchy : hierarchies ){
				category.addHierarchy( categoryHierarchy );
				
				List<CategoryLevel> levels = categoryLevelDao.fetchByHierarchyId( categoryHierarchy.getId() );
				for ( CategoryLevel categoryLevel : levels){
					categoryHierarchy.addLevel( categoryLevel );
				}
			}
		}
	}

	private void loadEquations(Workspace workspace) {
		equationManager.loadListsByWorkspace( workspace );
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
		if( samplingDesign != null ){
			workspace.setSamplingDesign( samplingDesign );
		}
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
	
	private void loadProcessingChains( Workspace workspace ){
		processingChainManager.loadChains( workspace );
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
		
		persistMetadata( workspace );
		
		return fetchWorkspaceByCollectSurveyUri( workspace.getCollectSurveyUri() );
	}
	/* 
	 * ===============================
	 *  Save metadata methods
	 * ===============================
	 */
	@Transactional
	private void persistMetadata( Workspace workspace ) {
		persistCategories( workspace );
		persistEntities( workspace );
	}
	
	@Transactional
	private void persistCategories(Workspace workspace) {
		List<Category> categories = workspace.getCategories();

		//remove persisted categories
		List<Category> persistedCategories = categoryDao.fetchByWorkspaceId( workspace.getId().longValue() );
		categoryDao.delete(persistedCategories);
		
		//save new categories
		for ( Category category : categories ){
			if( categoryDao.exists( category ) ){
				categoryDao.update(category);
			} else {
				category.setId( psql.nextval(Sequences.CATEGORY_ID_SEQ).intValue() );
				categoryDao.insert(category);
			}
			
			List<CategoryHierarchy> hierarchies = category.getHierarchies();
			for ( CategoryHierarchy hierarchy : hierarchies ){
				
				hierarchy.setCategoryId( category.getId().longValue() );
				
				if( categoryHierarchyDao.exists( hierarchy ) ){
					categoryHierarchyDao.update(hierarchy);
				} else {
					hierarchy.setId( psql.nextval(Sequences.CATEGORY_HIERARCHY_ID_SEQ).intValue() );
					categoryHierarchyDao.insert(hierarchy);
				}
				
				List<CategoryLevel> levels = hierarchy.getLevels();
				for ( CategoryLevel level : levels ){
					
					level.setHierarchyId( hierarchy.getId() );
					
					if( categoryLevelDao.exists( level ) ){
						categoryLevelDao.update(level);
					} else {
						level.setId( psql.nextval(Sequences.CATEGORY_LEVEL_ID_SEQ).intValue() );
						categoryLevelDao.insert(level);
					}	
				}
			}
		}
	}
	/**
	 * 
	 * @param workspace
	 */
	@Transactional
	private void persistEntities(Workspace workspace) {
		Collection<Entity> rootEntities = workspace.getRootEntities();
		for (Entity entity : rootEntities) {
			entity.traverse(new Entity.Visitor() {
				@Override
				public void visit(Entity entity) {
					// set parentEntityId
					Entity parent = entity.getParent();
					if ( parent != null && entity.getParentEntityId() == null ) {
						entity.setParentEntityId( parent.getId() );
					}
					// insert or update entity
					Integer id = entity.getId();
					if( id == null ) {
						entity.setId( psql.nextval( Sequences.ENTITY_ID_SEQ ).intValue() );
						entityDao.insert( entity );
					} else {
						entityDao.update( entity );
					}
					// save variables
					List<Variable<?>> varList = entity.getVariables();
					Variable<?>[] variables = varList.toArray( new Variable<?>[varList.size()] );
					variableDao.save( variables );
				}
			});
		}
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
	public void deleteEntities( Collection<Entity> entities ) {
		for ( Entity entity : entities ) {
			Workspace ws = entity.getWorkspace();
			List<Variable<?>> variables = entity.getVariables();
			deleteVariables(variables);
			
			entityDao.delete( entity );
			
			ws.removeEntity( entity );
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
		List<Workspace> list = findAllWorkspaces();
		for (Workspace ws : list) {
			ws.setActive( false );
			workspaceDao.update( ws );
		}
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
	
}

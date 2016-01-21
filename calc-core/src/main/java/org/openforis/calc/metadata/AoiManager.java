/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.AoiTable;
import org.openforis.calc.persistence.jooq.tables.daos.AoiHierarchyDao;
import org.openforis.calc.persistence.jooq.tables.daos.AoiLevelDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * 
 */
@Component
public class AoiManager {

	@Autowired
	private AoiHierarchyDao aoiHierarchyDao;

	@Autowired
	private AoiLevelDao aoiLevelDao;

	@Autowired
	private AoiDao aoiDao;

	@Autowired
	private Psql psql;
	
	@Transactional
	public void insert(Workspace workspace , AoiHierarchy aoiHierarchy) {
		delete( workspace );
		
		workspace.addAoiHierarchy(aoiHierarchy);
		aoiHierarchyDao.insert( aoiHierarchy );
		
		for (AoiLevel aoiLevel : aoiHierarchy.getLevels()) {
			aoiLevelDao.insert(aoiLevel);
			
			aoiDao.insert(aoiLevel.getAois());
		}
		
		loadByWorkspace(workspace);
	}
	
	@Transactional
	public void loadByWorkspace( Workspace workspace ) {
		// clear workspace aois first
		workspace.setAoiHierarchies( null );
		// then loads them
		List<AoiHierarchy> list = aoiHierarchyDao.fetchByWorkspaceId( workspace.getId() );
		for (AoiHierarchy aoiHierarchy : list) {
			workspace.addAoiHierarchy( aoiHierarchy );
			
			List<AoiLevel> aoiLevels = aoiLevelDao.fetchByAoiHierarchyId( aoiHierarchy.getId() );
			
			aoiHierarchy.setLevels( aoiLevels );
			
			AoiLevel rootLevel = aoiLevels.get(0);
			List<Aoi> aois = aoiDao.fetchByAoiLevelId( rootLevel.getId() );
			if( !aois.isEmpty() ) {
				Aoi root = aois.get(0);
				
				root.setAoiLevel( rootLevel );
				
				Collection<Aoi> children = loadAois( root );
				root.setChildren( children  );
				aoiHierarchy.setRootAoi(root);
			}
			
		}
	}

	private Collection<Aoi> loadAois(Aoi aoiParent) {
		List<Aoi> aois = aoiDao.fetchByParentAoiId( aoiParent.getId() );
		for (Aoi aoi : aois) {
			aoi.setAoiLevel( aoiParent.getAoiLevel().getHierarchy().getLevelById(aoi.getAoiLevelId()) );
			aoi.setParentAoi( aoiParent );

			Collection<Aoi> children = loadAois( aoi );
			aoi.setChildren( children  );
		}
		return aois;
	}
	
	/**
	 * Delete all Aoi Hierarchies/levels and aois for the given workspace
	 * @param workspace
	 */
	@Transactional
	public void delete( Workspace workspace ) {
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for ( AoiHierarchy aoiHierarchy : aoiHierarchies ) {
			
			Collection<AoiLevel> levels = aoiHierarchy.getLevelsReverseOrder();
			for ( AoiLevel aoiLevel : levels ) {
				AoiTable T = Tables.AOI;
				psql
					.delete( T )
					.where( T.AOI_LEVEL_ID.eq(aoiLevel.getId()) )
					.execute();
			}
			aoiLevelDao.delete( levels );
			
		}
		aoiHierarchyDao.delete( aoiHierarchies );
		
		workspace.setAoiHierarchies( new ArrayList<AoiHierarchy>() );
	}

	@Transactional
	public void importBackup( Workspace workspace, WorkspaceBackup workspaceBackup ) {
		List<AoiHierarchy> aoiHierarchies = workspaceBackup.getWorkspace().getAoiHierarchies();
		for ( AoiHierarchy aoiHierarchy : aoiHierarchies ) {
			this.createFromBackup( workspace , workspaceBackup , aoiHierarchy );
		}
	}
	
	@Transactional
	private void createFromBackup( Workspace workspace , WorkspaceBackup workspaceBackup, AoiHierarchy aoiHierarchy ) {
		
		aoiHierarchy.setId( psql.nextval(Sequences.AOI_HIERARCHY_ID_SEQ).intValue() );
		workspace.addAoiHierarchy(aoiHierarchy);
		aoiHierarchyDao.insert(aoiHierarchy);
		
		List<AoiLevel> levels = aoiHierarchy.getLevels();
		for ( AoiLevel aoiLevel : levels ) {
			aoiLevel.setId(  psql.nextval(Sequences.AOI_LEVEL_ID_SEQ).intValue()  );
			aoiLevel.setHierarchy(aoiHierarchy);
			
			aoiLevelDao.insert(aoiLevel);
		}
		
		Map<Integer, Integer> aoiIds = new HashMap<Integer, Integer>();
		
		Aoi aoi = aoiHierarchy.getRootAoi();
		createAoiFromBackup( aoi, levels, aoiIds, 0 );
		
		workspaceBackup.setAoiIdsMap( aoiIds );
		
		// replace aoiIds in error settings
		ErrorSettings errorSettings = workspaceBackup.getWorkspace().getErrorSettings();
		if( errorSettings != null ){
			for ( String key : errorSettings.getParameters().keys() ) {
				long variableId = Long.parseLong(key);
				Collection<? extends Number> aois = errorSettings.getAois( variableId );
				List<Long> newAoiIds = new ArrayList<Long>();
				for ( Number aoiId : aois ) {
					long newAoiId = aoiIds.get( aoiId.intValue() ).longValue();
					newAoiIds.add( newAoiId );
				}
				errorSettings.setAois( variableId, newAoiIds );
			}
		}
	}
	
	@Transactional
	private void createAoiFromBackup( Aoi aoi , List<AoiLevel> levels , Map<Integer, Integer> aoiIds , int indexLevel ) {
		// set aoi level
		AoiLevel aoiLevel = levels.get( indexLevel );
		aoi.setAoiLevel( aoiLevel );
		
		// set new aoi id and keep reference of old id
		Integer aoiId = aoi.getId();
		int newAoiId = psql.nextval( Sequences.AOI_ID_SEQ ).intValue();
		aoiIds.put( aoiId, newAoiId );
		aoi.setId( newAoiId );
		
		// set aoi parent id (taken from aoiIds map)
		Integer parentAoiId = aoi.getParentAoiId();
		if( parentAoiId != null ){
			Integer newParentAoiId = aoiIds.get(parentAoiId);
			aoi.setParentAoiId(newParentAoiId);
		}
		
		aoiDao.insert( aoi );
		
		// create child aois
		for ( Aoi childAoi : aoi.getChildren() ) {
			createAoiFromBackup( childAoi, levels, aoiIds, indexLevel+1 );
		}
		
	}

}

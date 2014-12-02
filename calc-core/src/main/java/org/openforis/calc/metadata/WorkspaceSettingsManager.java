/**
 * 
 */
package org.openforis.calc.metadata;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.WorkspaceSettingsTable;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceSettingsDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 *
 */
@Repository
public class WorkspaceSettingsManager {
	
	@Autowired
	private Psql psql;
	@Autowired
	private WorkspaceSettingsDao workspaceSettingsDao;
	
	@Transactional
	public void load( Workspace workspace ){
		
		WorkspaceSettings settings = workspaceSettingsDao.fetchOne( WorkspaceSettingsTable.WORKSPACE_SETTINGS.WORKSPACE_ID , workspace.getId().longValue() );
		
		if( settings == null ){
			settings = getDefaultSettings();
		}
	
		workspace.setSettings( settings );
	}

	private WorkspaceSettings getDefaultSettings() {
		WorkspaceSettings settings = new WorkspaceSettings();
		settings.setViewSteps( VIEW_STEPS.AS_LIST );
		return settings;
	}
	
	@Transactional
	public void save(  Workspace workspace ){
		WorkspaceSettings settings = workspace.getSettings();
		
		if( settings == null ){
			settings = getDefaultSettings();
			workspace.setSettings( settings );
		}
		
		if( settings.getId() == null ){
			long id = psql.nextval( Sequences.WORKSPACE_SETTINGS_ID_SEQ ).longValue();
			settings.setId(id);
			workspaceSettingsDao.insert( settings );
		} else {
			workspaceSettingsDao.update( settings );
		}
	}

	@Transactional
	public void delete( Workspace workspace ){
		WorkspaceSettingsTable table = WorkspaceSettingsTable.WORKSPACE_SETTINGS;
		psql
			.delete( table )
			.where( table.WORKSPACE_ID.eq(workspace.getId().longValue()) )
			.execute();
		
		workspace.setSettings( null );
	}

	@Transactional
	public void importBackup( Workspace workspace, WorkspaceBackup workspaceBackup ){
		WorkspaceSettings settings = workspaceBackup.getWorkspace().getSettings();
		if( settings != null ){
			settings.setId( null );
		}
		workspace.setSettings( settings );
		
		save( workspace );
	}
	

}

/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.ErrorSettingsTable;
import org.openforis.calc.persistence.jooq.tables.daos.ErrorSettingsDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 *
 */
@Repository
public class ErrorSettingsManager {
	
	@Autowired
	private Psql psql;
	@Autowired
	private ErrorSettingsDao errorSettingsDao;
	
	public void load( Workspace workspace ){
		ErrorSettings errorSettings = errorSettingsDao.fetchOne( ErrorSettingsTable.ERROR_SETTINGS.WORKSPACE_ID , workspace.getId().longValue() );
		if( errorSettings != null ){
			workspace.setErrorSettings( errorSettings );
		}
	}
	
	@Transactional
	public void save(  Workspace workspace ){
		ErrorSettings errorSettings = workspace.getErrorSettings();
		
		delete( workspace );

		if( errorSettings != null ){
			long id = psql.nextval( Sequences.ERROR_SETTINGS_ID_SEQ ).longValue();
			errorSettings.setId(id);
			errorSettingsDao.insert(errorSettings);
		}
	}

	@Transactional
	public void delete( Workspace workspace ){
		ErrorSettingsTable table = ErrorSettingsTable.ERROR_SETTINGS;
		psql
			.delete( table )
			.where( table.WORKSPACE_ID.eq(workspace.getId().longValue()) )
			.execute();
		
		workspace.setErrorSettings( null );
	}

	@Transactional
	public void importBackup( Workspace workspace, WorkspaceBackup workspaceBackup ){
		workspace.setErrorSettings( workspaceBackup.getWorkspace().getErrorSettings() );
		save( workspace );
	}
	
	@Transactional
	public void removeVariable( Workspace workspace , Variable<?> variable ){
		ErrorSettings errorSettings = workspace.getErrorSettings();
		if( variable instanceof QuantitativeVariable ){
			
			errorSettings.removeParameters( variable.getId().longValue() );
			
		} else if( variable instanceof CategoricalVariable<?> ){
			
			Set<String> keys = errorSettings.getParameters().keys();
			for ( String key : keys ){
				
				Collection<? extends Number> categoricalVariables = errorSettings.getCategoricalVariables( Long.parseLong(key) );
				for ( Iterator<? extends Number> iterator = categoricalVariables.iterator() ; iterator.hasNext() ; ){
					Long categoricalVariableId = iterator.next().longValue();
					if( categoricalVariableId.equals( variable.getId().longValue() ) ){
						iterator.remove();
					}
				}
				
			}
		}
		
		save( workspace );
	}
}

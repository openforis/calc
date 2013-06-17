package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Component
public class ContextManager {
//	@Autowired
//	private WorkspaceManager workspaceManager;
	@Autowired
	private DataSource userDataSource;
	
	public Context getContext(Workspace workspace) {
//		Workspace workspace = workspaceManager.getWorkspace(workspaceId);  
		return new Context(workspace, userDataSource);
	}
}

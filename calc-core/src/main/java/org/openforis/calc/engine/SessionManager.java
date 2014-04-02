/**
 * 
 */
package org.openforis.calc.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author S. Ricci
 *
 */
@Service
@Scope( WebApplicationContext.SCOPE_SESSION )
public class SessionManager {

	@Autowired
	private WorkspaceService workspaceService;

	private Workspace workspace;

	public Workspace getWorkspace() {
		if ( workspace == null ) {
			workspace = workspaceService.fetchActiveWorkspace();
		}
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}


	
}

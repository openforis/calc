package org.openforis.calc.engine;

import org.springframework.stereotype.Component;

/**
 * Manages workspaces and related locking 
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Component
public class WorkspaceManager {

	public void lock(int workspaceId) throws WorkspaceLockedException {
		
	}
	
	public void unlock(int workspaceId) {
		
	}
	
	public boolean isLocked(int workspaceId) {
		// TODO Auto-generated method stub
		// TODO?
		return false;
	}
	
	
}

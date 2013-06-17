package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Manages workspaces and related locking 
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Component
public class WorkspaceLockManager {
	
	private Set<Integer> lockedWorkspaces; 

	public WorkspaceLockManager() {
		this.lockedWorkspaces = new HashSet<Integer>();
	}
	
	synchronized
	public void lock(int workspaceId) throws WorkspaceLockedException {
		if ( lockedWorkspaces.contains(workspaceId) ) {
			throw new WorkspaceLockedException();
		} else {
			lockedWorkspaces.add(workspaceId);
		}
	}

	synchronized
	public void unlock(int workspaceId) {
		lockedWorkspaces.remove(workspaceId);
	}
}

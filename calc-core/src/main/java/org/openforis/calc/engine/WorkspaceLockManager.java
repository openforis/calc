package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.Map;

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
	
	private Map<Integer, SimpleLock> locks; 

	public WorkspaceLockManager() {
		this.locks = new HashMap<Integer, SimpleLock>();
	}
	
	synchronized
	public SimpleLock lock(int workspaceId) throws WorkspaceLockedException {
		SimpleLock lock = locks.get(workspaceId);
		if ( lock == null ) {
			lock = new SimpleLock();
			locks.put(workspaceId, lock);
		}
		if ( !lock.tryLock() ) {
			throw new WorkspaceLockedException();
		}
		return lock;
	}
}

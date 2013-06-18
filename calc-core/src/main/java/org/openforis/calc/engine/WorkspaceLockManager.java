package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private Map<Integer, Lock> locks; 

	public WorkspaceLockManager() {
		this.locks = new HashMap<Integer, Lock>();
	}
	
	synchronized
	public Lock lock(int workspaceId) throws WorkspaceLockedException {
		Lock lock = locks.get(workspaceId);
		if ( lock == null ) {
			lock = new ReentrantLock();
			locks.put(workspaceId, lock);
		}
		if ( !lock.tryLock() ) {
			throw new WorkspaceLockedException();
		}
		return lock;
	}
}

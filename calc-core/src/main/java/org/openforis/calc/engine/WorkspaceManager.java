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
public class WorkspaceManager {
	
	private Map<Integer, Lock> locks; 

	public WorkspaceManager() {
		this.locks = new HashMap<Integer, Lock>();
	}
	
	private Lock getLock(int workspaceId) {
		Lock lock = locks.get(workspaceId);
		if ( lock == null ) {
			lock = new ReentrantLock();
			locks.put(workspaceId, lock);
		}
		return lock;
	}
	
	synchronized
	public void lock(int workspaceId) throws WorkspaceLockedException {
		Lock lock = getLock(workspaceId);
		if ( !lock.tryLock() ) {
			throw new WorkspaceLockedException();
		}
	}

	synchronized
	public void unlock(int workspaceId) throws WorkspaceLockedException {
		Lock lock = getLock(workspaceId);
		lock.unlock();
	}
}

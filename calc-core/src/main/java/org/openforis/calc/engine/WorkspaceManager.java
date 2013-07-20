package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Component
public class WorkspaceManager {
	@Autowired
	private WorkspaceDao dao;

	private Map<Integer, SimpleLock> locks; 

	public WorkspaceManager() {
		this.locks = new HashMap<Integer, SimpleLock>();
	}

	@Transactional
	public Workspace get(int workspaceId) {
		return dao.find(workspaceId);
	}

	@Transactional
	public Workspace fetchByName(String name) {
		return dao.findByName(name);
	}
	
	@Transactional
	public Workspace save(Workspace workspace) {
		return dao.save(workspace);
	}

	public List<Workspace> loadAll() {
		return dao.loadAll();
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
	
	synchronized
	public boolean isLocked(int workspaceId) {
		SimpleLock lock = locks.get(workspaceId);
		if ( lock == null ) {
			return false;
		} else {
			return lock.isLocked(); 
		}
	}
}

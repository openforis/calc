package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
public class WorkspaceService {

	@Autowired
	private WorkspaceDao dao;

	@Autowired
	private ProcessingChainService processingChainService;

	private Map<Integer, SimpleLock> locks;

	public WorkspaceService() {
		this.locks = new HashMap<Integer, SimpleLock>();
	}

	@Transactional
	public Workspace get(int workspaceId) {
		return dao.find(workspaceId);
	}

	@Transactional
	public Workspace fetchByName(String name) {
		return dao.fetchByName(name);
	}

	@Transactional
	public Workspace fetchCollectSurveyUri(String uri) {
		return dao.fetchByCollectSurveyUri(uri);
	}

	@Transactional
	public Workspace save(Workspace workspace) {
		return dao.save(workspace);
	}

	@Transactional
	public List<Workspace> loadAll() {
		return dao.loadAll();
	}

	/**
	 * It returns the active workspace
	 * 
	 * @return
	 */
	public Workspace getActiveWorkspace() {
		Workspace workspace = dao.fetchActive();
		return workspace;
	}

	synchronized public SimpleLock lock(int workspaceId) throws WorkspaceLockedException {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			lock = new SimpleLock();
			locks.put(workspaceId, lock);
		}
		if (!lock.tryLock()) {
			throw new WorkspaceLockedException();
		}
		return lock;
	}

	synchronized public boolean isLocked(int workspaceId) {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			return false;
		} else {
			return lock.isLocked();
		}
	}

	public Workspace createAndActivateWorkspace(String name, String uri, String schema) {
		dao.deactivateAll();

		Workspace ws = new Workspace();
		ws.setActive(true);
		ws.setCollectSurveyUri(uri);
		ws.setInputSchema(schema);
		ws.setName(name);
		ws.setCaption(name);
		dao.save(ws);

		processingChainService.createDefaultProcessingChain(ws);

		return ws;
	}
}

package org.openforis.calc.engine;

import java.util.List;

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
	private WorkspaceDao workspaceDao;

	public WorkspaceManager() {
	}
	
	@Transactional
	public Workspace getWorkspace(int workspaceId) {
		return workspaceDao.find(workspaceId);
	}
	
	@Transactional
	public Workspace saveWorkspace(Workspace workspace) {
		return workspaceDao.save(workspace);
	}
	
	public List<Workspace> loadAll(){
		return workspaceDao.loadAll();
	}
}
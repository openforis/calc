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
	private WorkspaceDao dao;

	public WorkspaceManager() {
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
}

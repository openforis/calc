package org.openforis.calc.engine;


import org.openforis.calc.persistence.ObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages persistence and creation of {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
public class WorkspaceService extends ObjectManager<Workspace>{
//	@Autowired
//	private EntityManager entityManager;
	@Autowired
	private WorkspaceDao workspaceDao;
	
	public WorkspaceService() {
		super(Workspace.class);
	}
	
	public Workspace getWorkspace(int workspaceId) {
		Workspace w = workspaceDao.find(workspaceId);
//		Workspace w = createJooqDao().findById(workspaceId);
//		List<Entity> entities = entityManager.getEntities(workspaceId);
//		w.setEntities(entities);
		// TODO processing chains
		return w;
	}
	
	@Transactional
	public Workspace saveWorkspace(Workspace workspace) {
		return workspaceDao.save(workspace);
	}
}
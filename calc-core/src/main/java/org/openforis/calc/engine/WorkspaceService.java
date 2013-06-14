package org.openforis.calc.engine;


import org.openforis.calc.persistence.ObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages persistence and creation of {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
public final class WorkspaceService extends ObjectManager<Workspace>{
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
}
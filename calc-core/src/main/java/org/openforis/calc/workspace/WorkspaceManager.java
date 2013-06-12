package org.openforis.calc.workspace;

import org.openforis.calc.persistence.jooq.JooqDaoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manages persistence and creation of {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Component
public final class WorkspaceManager {
	@Autowired
	private JooqDaoFactory daoFactory;
	
	public Workspace getWorkspace(int workspaceId) {
		Workspace w = daoFactory.createJooqDao(Workspace.class).findById(workspaceId);
		
		return w;
	}
}
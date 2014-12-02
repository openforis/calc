/**
 * 
 */
package org.openforis.calc.engine;

import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Task responsible for deleting a workspace 
 * 
 * @author Mino Togna
 *
 */
public class DeleteWorkspaceTask extends Task {

	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Override
	protected long countTotalItems() {
		return 1;
	}
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		
		workspaceDao.delete( workspace );
		incrementItemsProcessed();
	}
	
	@Override
	public String getName() {
		return "Delete "  + getWorkspace().getName() + " workspace";
	}

}

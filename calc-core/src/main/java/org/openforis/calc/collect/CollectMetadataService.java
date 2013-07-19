package org.openforis.calc.collect;

import org.openforis.calc.engine.ContextManager;
import org.openforis.calc.engine.TaskContext;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CollectMetadataService {
	
	@Autowired
	private WorkspaceManager workspaceManager;
	@Autowired
	private ContextManager contextManager;
	@Autowired 
	private TaskManager taskManager;

	public void importMetadata() throws WorkspaceLockedException {

		//TODO get the workspace somewhere else...
		Workspace ws = workspaceManager.fetchByName("test");
		
		TaskContext context = contextManager.createContext(ws);
		CollectMetadataImportTask task= taskManager.createTask(CollectMetadataImportTask.class, context);
		taskManager.start(task);
	}

}

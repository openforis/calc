package org.openforis.calc.collect;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
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
	private WorkspaceService workspaceManager;
	@Autowired 
	private TaskManager taskManager;

	public Job startSync(int workspaceId) throws WorkspaceLockedException {
		Workspace ws = workspaceManager.get(workspaceId);
		Job job = createSyncJob(ws);
		taskManager.startJob(job);
		return job;
	}

	private Job createSyncJob(Workspace workspace) {
		Job job = taskManager.createJob(workspace);
		SyncMetadataTask syncMetadata = taskManager.createTask(SyncMetadataTask.class);
		job.addTask(syncMetadata);
		SyncCategoriesTask syncCategories = taskManager.createTask(SyncCategoriesTask.class);
		job.addTask(syncCategories);
		return job;
	}

}

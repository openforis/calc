package org.openforis.calc.collect;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:applicationContext.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class CollectMetadataServiceIntegrationTest {

	@Autowired
	private CollectMetadataService collectMetadataService;
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Autowired
	private TaskManager taskManager;
	
	/*
	@Test
	public void testCollectMetadataSync() throws WorkspaceLockedException {
//		Workspace ws = new Workspace();
//		ws.setName("test2");
//		ws.setInputSchema("naforma1");
//		ws.setOutputSchema("calc");
//		workspaceService.save(ws);
		
		Job job = collectMetadataService.startSync(2);
		boolean ok = job.waitFor(30000);
		Assert.assertTrue(ok);
	}
	*/
	
	@Test
	public void testCollectMetadataSyncTasks() throws WorkspaceLockedException {
		Workspace ws = new Workspace();
		ws.setId(-1); //allows locking...
		ws.setName("test");
		ws.setInputSchema("naforma1");
		ws.setOutputSchema("calc");
//		workspaceManager.save(ws);
//		Integer workspaceId = ws.getId();
//		Job job = collectMetadataService.startSync(workspaceId);
//		Job job = collectMetadataService.startSync(174);
		
		Job job = taskManager.createJob(ws);
		SaveWorkspaceTask saveWorkspaceTask = taskManager.createTask(SaveWorkspaceTask.class);
		job.addTask(saveWorkspaceTask);
		SyncMetadataTask syncMetadata = taskManager.createTask(SyncMetadataTask.class);
		job.addTask(syncMetadata);
		SyncCategoriesTask syncCategories = taskManager.createTask(SyncCategoriesTask.class);
		job.addTask(syncCategories);
		taskManager.startJob(job);
		
		boolean ok = job.waitFor(30000);
		Assert.assertTrue(ok);
	}
	
	public static class SaveWorkspaceTask extends Task {
		
		@Autowired
		private WorkspaceService workspaceService;
		
		@Override
		protected void execute() throws Throwable {
			super.execute();
			Workspace ws = getContext().getWorkspace();
			ws.setId(null);
			workspaceService.save(ws);
		}
		
	}
}

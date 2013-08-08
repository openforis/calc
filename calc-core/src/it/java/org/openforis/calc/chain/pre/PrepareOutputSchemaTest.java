package org.openforis.calc.chain.pre;

import org.junit.Test;
import org.openforis.calc.chain.post.UpdateExpFactorTask;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceDao;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 * 
 */
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class PrepareOutputSchemaTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private WorkspaceDao workspaceDao;

	@Test
	public void testRun() throws WorkspaceLockedException {
		Workspace foundWorkspace = workspaceDao.find(1);
		Job job = taskManager.createUserJob(foundWorkspace);
		job.addTask(DropOutputSchemaTask.class);
		job.addTask(CreateOutputSchemaTask.class);
		job.addTask(CreateCategoryDimensionTablesTask.class);
		job.addTask(CreateAoiDimensionTablesTask.class);
		job.addTask(CreateFactTablesTask.class);
		job.addTask(CreateLocationColumnsTask.class);
		job.addTask(CreateAoiColumnsTask.class);
		job.addTask(CreateStratumDimensionTask.class);
		job.addTask(UpdateExpFactorTask.class);
		job.addTask(OutputSchemaGrantsTask.class);
		taskManager.startJob(job);
		job.waitFor(5000);
	}
}

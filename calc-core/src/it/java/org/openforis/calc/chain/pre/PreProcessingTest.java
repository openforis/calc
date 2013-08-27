package org.openforis.calc.chain.pre;

import org.junit.Test;
import org.openforis.calc.chain.post.CreateFactTablesTask;
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
public class PreProcessingTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private WorkspaceDao workspaceDao;

	@Test
	public void testRun() throws WorkspaceLockedException {
		Workspace foundWorkspace = workspaceDao.find(1);
		Job job = taskManager.createUserJob(foundWorkspace);
		job.addTasks(taskManager.createTasks(
//				ResetOutputSchemaTask.class,
				CreateCategoryDimensionTablesTask.class
//				CreateAoiDimensionTablesTask.class,
//				CreateStratumDimensionTableTask.class,				
//				CreateDataTablesTask.class,
//				AssignLocationColumnsTask.class,
//				AssignAoiColumnsTask.class
				));
		taskManager.startJob(job);
		job.waitFor(5000);
	}
}

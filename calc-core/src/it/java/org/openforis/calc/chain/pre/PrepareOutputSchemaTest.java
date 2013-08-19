package org.openforis.calc.chain.pre;

import java.util.List;

import org.junit.Test;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
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
		List<Task> tasks = taskManager.createTasks(
				DropOutputSchemaTask.class,
				CreateOutputSchemaTask.class,
				CreateCategoryDimensionTablesTask.class,
				CreateAoiDimensionTablesTask.class,
				CreateOutputTablesTask.class,
				CreateLocationColumnsTask.class,
				CreateAoiColumnsTask.class,
				CreateStratumDimensionTableTask.class,
				OutputSchemaGrantsTask.class);
		Job job = taskManager.createUserJob(foundWorkspace, tasks);
		taskManager.startJob(job);
		job.waitFor(5000);
	}
}

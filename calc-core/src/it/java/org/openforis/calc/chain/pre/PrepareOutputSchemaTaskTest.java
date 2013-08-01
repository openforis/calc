package org.openforis.calc.chain.pre;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openforis.calc.chain.pre.CreateOutputSchemaTask;
import org.openforis.calc.chain.pre.DropOutputSchemaTask;
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
public class PrepareOutputSchemaTaskTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	TaskManager taskManager;
	@Autowired
	WorkspaceDao workspaceDao;
	Job job;

	@Before
	public void setUpBeforeClass() throws Exception {

		Workspace foundWorkspace = workspaceDao.find(1);
		job = taskManager.createJob(foundWorkspace);
		job.addTask(DropOutputSchemaTask.class);
		job.addTask(CreateOutputSchemaTask.class);
		job.addTask(CreateCategoryDimensionTablesTask.class);
		job.addTask(OutputSchemaGrantsTask.class);
	}

	@Test
	public void testRun() throws WorkspaceLockedException {
		taskManager.startJob(job);
		job.waitFor(5000);
	}
}

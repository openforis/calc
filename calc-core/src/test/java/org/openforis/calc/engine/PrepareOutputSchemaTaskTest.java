package org.openforis.calc.engine;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class PrepareOutputSchemaTaskTest extends AbstractTransactionalJUnit4SpringContextTests {


	@Autowired
	TaskManager taskManager;

	@Autowired
	WorkspaceDao workspaceDao;

	Task taskToTest;

	Job job;

	@Before
	public void setUpBeforeClass() throws Exception {

		taskToTest = taskManager.createTask(PrepareOutputSchemaTask.class);
		Workspace foundWorkspace = workspaceDao.find(87);
		job = taskManager.createJob(foundWorkspace);
		job.addTask(taskToTest);

	}

	@Test
	public void testRun() throws WorkspaceLockedException {

		taskManager.startJob(job);
		job.waitFor(5000);

	}

	@Test
	public void testGetContext() {
		fail("Not yet implemented");
	}

}

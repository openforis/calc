package org.openforis.calc.engine;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openforis.calc.chain.pre.CreateFactTablesTask;
import org.openforis.calc.chain.pre.CreateInputSchemaTask;
import org.openforis.calc.chain.pre.DropOutputSchemaTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

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
		job.addTask(CreateInputSchemaTask.class);
		job.addTask(CreateFactTablesTask.class);

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

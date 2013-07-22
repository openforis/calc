package org.openforis.calc.engine;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class PreprocessInputSchemaTaskTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	TaskContext taskContext;

	@Autowired
	ContextManager contextManager;

	Task taskToTest;

	@Before
	public void setUpBeforeClass() throws Exception {
		Workspace workspace = new Workspace();
		workspace.setDescription("Test Workspace");
		workspace.setId(111);
		workspace.setInputSchema("inputSchema");
		workspace.setName("Test Workspace");
		workspace.setOutputSchema("outputSchema");
		taskToTest = Task.createTask(PreprocessInputSchemaTask.class, contextManager.getContext(workspace));
	}

	@Test
	public void testRun() {

		taskToTest.run();
		
		fail("Not yet implemented");
	}

	@Test
	public void testGetContext() {
		fail("Not yet implemented");
	}

}

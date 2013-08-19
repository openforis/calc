package org.openforis.calc.chain.pre;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.openforis.calc.chain.post.CreateFactTablesTask;
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
 * @author G. Miceli
 * 
 */
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class PostProcessingTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private WorkspaceDao workspaceDao;
	@Autowired
	private DataSource userDataSource;
	
	@Test
	public void testRun() throws WorkspaceLockedException {
		Workspace ws = workspaceDao.find(1);
		List<Task> tasks = taskManager.createTasks(CreateFactTablesTask.class, CreateFactTablesTask.class);
		Job job = taskManager.createUserJob(ws, tasks);
//		job.addTask(AddMissingAggregateColumnsTask.class);
//		job.addTask(CalculateExpansionFactorsTask.class);
//		job.addTask(CreateFactTablesTask.class);
//		job.addTask(GenerateRolapSchemaTask.class);
		taskManager.startJob(job);
		job.waitFor(5000);
	}
}

package org.openforis.calc.chain.pre;

//import java.util.List;

import org.junit.Test;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.chain.post.AssignDimensionIdsTask;
import org.openforis.calc.chain.post.AssignStratumIdsTask;
import org.openforis.calc.chain.post.CalculateExpansionFactorsTask;
import org.openforis.calc.chain.post.CreateAggregateTablesTask;
import org.openforis.calc.chain.post.CreateFactTablesTask;
import org.openforis.calc.chain.post.PublishRolapSchemaTask;
import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 * @author M. Togna
 *
 */
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class ProcessingChainTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private CalculationEngine calculationEngine;
	@Autowired
	private ProcessingChainDao processingChainDao;
	@Autowired
	private TaskManager taskManager;

//	@Test
	public void testTasks() throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(21);
		Workspace workspace = chain.getWorkspace(); 
		Job job = taskManager.createUserJob(workspace);
		
//		// Preprocessing steps
//		job.addTask(taskManager.createTask(ResetOutputSchemaTask.class));
//		job.addTask(taskManager.createTask(CreateCategoryDimensionTablesTask.class));
//		job.addTask(taskManager.createTask(CreateAoiDimensionTablesTask.class));
//		job.addTask(taskManager.createTask(CreateStratumDimensionTableTask.class));
//		job.addTask(taskManager.createTask(CreateOutputTablesTask.class));
//		job.addTask(taskManager.createTask(ApplyDefaultsTask.class));
//		job.addTask(taskManager.createTask(AssignLocationColumnsTask.class));
//		job.addTask(taskManager.createTask(AssignAoiColumnsTask.class));
//		
//		// User steps
//		job.addTasks(taskManager.createCalculationStepTasks(chain));
//		
//		// Postprocessing
//		job.addTask(taskManager.createTask(CreateFactTablesTask.class));
//		job.addTask(taskManager.createTask(AssignStratumIdsTask.class));
//		job.addTask(taskManager.createTask(AssignDimensionIdsTask.class));
//		job.addTask(taskManager.createTask(CalculateExpansionFactorsTask.class));
//		job.addTask(taskManager.createTask(CreateAggregateTablesTask.class));
		job.addTask(taskManager.createTask(PublishRolapSchemaTask.class));
		
		
		taskManager.startJob(job);
		job.waitFor(5000);
	}
	
//	@Test
	public void testPreProcessing() throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(21);
		Workspace workspace = chain.getWorkspace(); 
		Job job = taskManager.createUserJob(workspace);
		job.addTasks(taskManager.createTasks( CalculationEngine.PREPROCESSING_TASKS) );
		taskManager.startJob(job);
		job.waitFor(5000);
	}
	
	@Test
	public void testPostProcessing() throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(21);
		Workspace workspace = chain.getWorkspace(); 
		Job job = taskManager.createUserJob(workspace);
		job.addTasks( taskManager.createTasks(CalculationEngine.POSTPROCESSING_TASKS) );
		
		taskManager.startJob(job);
		job.waitFor(5000);
	}
	
	@Test
	public void testChain() throws WorkspaceLockedException, InvalidProcessingChainException {
		Job job = calculationEngine.runProcessingChain(21);
		while(!job.isEnded()){
			job.waitFor(10000);
			
		}
	}
}

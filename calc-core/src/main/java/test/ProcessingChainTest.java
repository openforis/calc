package test;

//import java.util.List;

import java.util.List;

import org.junit.Test;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.chain.post.PublishRolapSchemaTask;
import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.CalculationStepTask;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Worker;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.module.r.CustomRTask;
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
public class ProcessingChainTest 
//{ 	
extends AbstractTransactionalJUnit4SpringContextTests {
	
	@Autowired
	private CalculationEngine calculationEngine;
	@Autowired
	private ProcessingChainDao processingChainDao;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private CalculationStepDao calculationStepDao;
	@Autowired
	private WorkspaceService workspaceService;
	
//	@Test
	public void testTasks() throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(21);
		Workspace workspace = chain.getWorkspace(); 
		Job job = taskManager.createJob(workspace);
		
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
		Job job = taskManager.createJob(workspace);
		job.addTasks(taskManager.createTasks( CalculationEngine.PREPROCESSING_TASKS) );
		taskManager.startJob(job);
		job.waitFor(5000);
	}
	
//	@Test
	public void testPostProcessing() throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(21);
		Workspace workspace = chain.getWorkspace(); 
		Job job = taskManager.createJob(workspace);
		job.addTasks( taskManager.createTasks(CalculationEngine.POSTPROCESSING_TASKS) );
		
		taskManager.startJob(job);
		job.waitFor(5000);
	}
	
//	@Test
	public void testChain() throws WorkspaceLockedException, InvalidProcessingChainException {
		Job job = calculationEngine.runProcessingChain(21);
		while(!job.isEnded()){
			job.waitFor(10000);
			
		}
	}

	@Test
	public void testStep() throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		CalculationStep step = calculationStepDao.find(1);
		CustomRTask task = (CustomRTask) taskManager.createCalculationStepTask(step);
		task.setMaxItems(18000);
		
		Job job = taskManager.createJob(workspace);
		job.addTask(task);
		
		taskManager.startJob(job);
		job.waitFor(5000);
		while(job.getStatus() !=Worker.Status.COMPLETED){
			//wait
		}
		System.out.println(task.getItemsProcessed());
		List<DataRecord> results = task.getAndResetResults();
		System.out.println(results.size());
	}
}

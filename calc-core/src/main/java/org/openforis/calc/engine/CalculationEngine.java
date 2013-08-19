package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.chain.post.AddMissingAggregateColumnsTask;
import org.openforis.calc.chain.post.CalculateExpansionFactorsTask;
import org.openforis.calc.chain.post.CreateFactTablesTask;
import org.openforis.calc.chain.pre.CreateAoiColumnsTask;
import org.openforis.calc.chain.pre.CreateAoiDimensionTablesTask;
import org.openforis.calc.chain.pre.CreateCategoryDimensionTablesTask;
import org.openforis.calc.chain.pre.CreateLocationColumnsTask;
import org.openforis.calc.chain.pre.CreateOutputSchemaTask;
import org.openforis.calc.chain.pre.CreateStratumDimensionTableTask;
import org.openforis.calc.chain.pre.DropOutputSchemaTask;
import org.openforis.calc.chain.pre.OutputSchemaGrantsTask;
import org.openforis.calc.metadata.task.UpdateSamplingUnitAoisTask;
import org.openforis.calc.module.ModuleRegistry;
import org.openforis.calc.module.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Service
public class CalculationEngine {
	
	@Autowired 
	private TaskManager taskManager;

	@Autowired
	private ModuleRegistry moduleRegistry;
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired
	private WorkspaceDao workspaceDao;

	private Class<?>[] PREPROCESSING_TASKS = {
			DropOutputSchemaTask.class,
			CreateOutputSchemaTask.class,
			CreateCategoryDimensionTablesTask.class,
			CreateAoiDimensionTablesTask.class,
			CreateStratumDimensionTableTask.class,
			CreateFactTablesTask.class,
			CreateLocationColumnsTask.class,
			CreateAoiColumnsTask.class,
			OutputSchemaGrantsTask.class};

	private Class<?>[] POSTPROCESSING_TASKS = { 
			CalculateExpansionFactorsTask.class,
			AddMissingAggregateColumnsTask.class,
			CreateFactTablesTask.class};

	synchronized
	public Job runProcessingChain(int chainId) throws WorkspaceLockedException, InvalidProcessingChainException {
		Job job = createProcessingChainJob(chainId);
		taskManager.startJob(job);
		return job;
	}

	synchronized	
	private Job createProcessingChainJob(int chainId) throws InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(chainId);
		if ( chain == null ) {
			throw new IllegalArgumentException("No processing chain with id "+chainId);
		}
		Workspace workspace = chain.getWorkspace();
		List<Task> tasks = new ArrayList<Task>();
		
		// Add preprocessing tasks
		
		tasks.addAll( taskManager.createTasks(PREPROCESSING_TASKS) );
		
		// Add steps to job
		List<CalculationStep> steps = chain.getCalculationSteps();
		for (CalculationStep step : steps) {
			Operation<?> operation = moduleRegistry.getOperation(step);
			if ( operation == null ) {
				throw new InvalidProcessingChainException("Unknown operation in step "+step);
			}
			Class<? extends CalculationStepTask> taskType = operation.getTaskType();
			CalculationStepTask task = taskManager.createTask(taskType);
			task.setCalculationStep(step);			
			tasks.add(task);
		}
		
		// Add preprocessing tasks
		tasks.addAll( taskManager.createTasks(POSTPROCESSING_TASKS) );
		
		Job job = taskManager.createUserJob(workspace, tasks);
		
		return job;
	}



	synchronized
	public Job updateStratumWeights(int workspaceId) throws WorkspaceLockedException {
		Workspace workspace = workspaceDao.find(workspaceId);
		List<Task> tasks = taskManager.createTasks(UpdateSamplingUnitAoisTask.class);
		Job job = taskManager.createSystemJob(workspace, tasks);
		taskManager.startJob(job);
		return job;
	}
}

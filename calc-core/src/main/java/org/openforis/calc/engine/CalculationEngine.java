package org.openforis.calc.engine;

import java.util.List;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.chain.post.CalculateExpansionFactorsTask;
import org.openforis.calc.chain.pre.CreateAoiColumnsTask;
import org.openforis.calc.chain.pre.CreateAoiDimensionTablesTask;
import org.openforis.calc.chain.pre.CreateCategoryDimensionTablesTask;
import org.openforis.calc.chain.pre.CreateFactTablesTask;
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
		Job job = taskManager.createUserJob(workspace);

		// Add preprocess steps to the job
//		addPreprocessingTasks(job);

		// Add steps to job
		List<CalculationStep> steps = chain.getCalculationSteps();
		addCalculationStepTasks(job, steps);
		
		addPostprocessingTasks(job);
		return job;
	}

	private void addPreprocessingTasks(Job job) {
		job.addTask(DropOutputSchemaTask.class);
		job.addTask(CreateOutputSchemaTask.class);
		job.addTask(CreateCategoryDimensionTablesTask.class);
		job.addTask(CreateAoiDimensionTablesTask.class);
		job.addTask(CreateStratumDimensionTableTask.class);
		job.addTask(CreateFactTablesTask.class);
		job.addTask(CreateLocationColumnsTask.class);
		job.addTask(CreateAoiColumnsTask.class);
		job.addTask(OutputSchemaGrantsTask.class);
	}

	private void addCalculationStepTasks(Job job, List<CalculationStep> steps)
			throws InvalidProcessingChainException {
		for (CalculationStep step : steps) {
			Operation<?> operation = moduleRegistry.getOperation(step);
			if ( operation == null ) {
				throw new InvalidProcessingChainException("Unknown operation in step "+step);
			}
			Class<? extends CalculationStepTask> taskType = operation.getTaskType();
			CalculationStepTask task = job.addTask(taskType);
			task.setCalculationStep(step);			
		}
	}

	private void addPostprocessingTasks(Job job) {
		// TODO Auto-generated method stub
		job.addTask(CalculateExpansionFactorsTask.class);
	}

	synchronized
	public Job updateStratumWeights(int workspaceId) throws WorkspaceLockedException {
		Workspace workspace = workspaceDao.find(workspaceId);
		Job job = taskManager.createSystemJob(workspace);
		job.addTask(UpdateSamplingUnitAoisTask.class);
		taskManager.startJob(job);
		return job;
	}
}

package org.openforis.calc.chain;

import java.util.List;

import org.openforis.calc.chain.pre.PrepareOutputSchemaTask;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.module.ModuleRegistry;
import org.openforis.calc.module.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
public class ProcessingChainService {
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired 
	private TaskManager taskManager;

	@Autowired
	private ModuleRegistry moduleRegistry;
	
	@Transactional
	public void saveProcessingChain(ProcessingChain chain) {
		processingChainDao.save(chain);
		// TODO update Workspace?
	}
	
	synchronized
	public Job startProcessingChainJob(int chainId) throws WorkspaceLockedException, InvalidProcessingChainException {
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
		Job job = taskManager.createJob(workspace);

		// Add preprocess steps to the job
		addPreprocessSteps(job);

		// Add steps to job
		List<CalculationStep> steps = chain.getCalculationSteps();
		for (CalculationStep step : steps) {
			Operation<?> operation = moduleRegistry.getOperation(step);
			if ( operation == null ) {
				throw new InvalidProcessingChainException();
			}
			Class<? extends CalculationStepTask> taskType = operation.getTaskType();
			CalculationStepTask task = job.addTask(taskType);
			task.setCalculationStep(step);			
		}
		return job;
	}

	private void addPreprocessSteps(Job job) {

		job.addTask(PrepareOutputSchemaTask.class);

	}
}

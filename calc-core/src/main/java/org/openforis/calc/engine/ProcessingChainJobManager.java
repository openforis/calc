package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class ProcessingChainJobManager {
	@Autowired
	private ModuleRegistry moduleRegistry;

	@Autowired
	private TaskManager taskManager;
	
	@Autowired
	private ContextManager contextManager;

	@Autowired
	private ProcessingChainDao processingChainDao;
	
	private Map<Integer, Job> jobs;
	
	public ProcessingChainJobManager() {
		this.jobs = new HashMap<Integer, Job>();
	}
	
	public Job getProcessingChainJob(ProcessingChain chain) throws InvalidProcessingChainException {
		Integer chainId = chain.getId();
		Job job = jobs.get(chainId);
		if ( job == null ) {
			Workspace workspace = chain.getWorkspace();
			TaskContext context = contextManager.createContext(workspace);
			job = createProcessingChainJob(context, chain);
			jobs.put(chainId, job);
		}
		return job;
	}

	private List<Task> createTasks(ProcessingChain chain, TaskContext context) throws InvalidProcessingChainException {
		// Add steps to job
		List<CalculationStep> steps = chain.getCalculationSteps();
		List<Task> tasks = new ArrayList<Task>();
		for (CalculationStep step : steps) {
			Operation<?> operation = moduleRegistry.getOperation(step);
			if ( operation == null ) {
				throw new InvalidProcessingChainException();
			}			
			Class<? extends CalculationStepTask> taskType = operation.getTaskType();
			CalculationStepTask task = taskManager.createTask(taskType, context);
			task.setCalculationStep(step);
			tasks.add(task);
		}
		return tasks;
	}

	private Job createProcessingChainJob(TaskContext context, ProcessingChain chain) throws InvalidProcessingChainException {
		List<Task> tasks = createTasks(chain, context);
		// add chain-level parameters?
		Job job = new Job(tasks);
		return job;
	}
}

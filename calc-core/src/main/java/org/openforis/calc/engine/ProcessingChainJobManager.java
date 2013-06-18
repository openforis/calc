package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
	private ContextManager contextManager;

	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired 
	private TaskManager taskManager;
	
	@Autowired
	private ModuleRegistry moduleRegistry;
	
	private Map<Integer, ProcessingChainJob> jobs;
	
	public ProcessingChainJobManager() {
		this.jobs = new HashMap<Integer, ProcessingChainJob>();
	}
	
	public ProcessingChainJob getProcessingChainJob(ProcessingChain chain) throws InvalidProcessingChainException {
		Integer chainId = chain.getId();
		ProcessingChainJob job = jobs.get(chainId);
		if ( job == null ) {
			Workspace workspace = chain.getWorkspace();
			Context context = contextManager.getContext(workspace);
			job = createProcessingChainJob(context, chain);
			jobs.put(chainId, job);
		}
		return job;
	}

	private ProcessingChainJob createProcessingChainJob(Context context, ProcessingChain chain) throws InvalidProcessingChainException {
		// add chain-level parameters?
		ProcessingChainJob job;
		job = Task.createTask(ProcessingChainJob.class, context);
		List<CalculationStep> steps = chain.getCalculationSteps();
		for (CalculationStep step : steps) {
			Operation<?> operation = moduleRegistry.getOperation(step);
			if ( operation == null ) {
				throw new InvalidProcessingChainException();
			}
			CalculationStepTask task = operation.createTask(context, step.parameters());
			job.addTask(task);
		}
		return job;
	}

	synchronized
	public void startProcessingChainJob(ProcessingChainJob job, Set<UUID> taskIds) throws WorkspaceLockedException {
		Context ctx = job.getContext();
		ctx.setScheduledTasks(taskIds);
		taskManager.start(job);
	}
}

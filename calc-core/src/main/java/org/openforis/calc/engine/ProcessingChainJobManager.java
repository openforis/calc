package org.openforis.calc.engine;

import java.util.HashMap;
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
	
	private Map<Integer, ProcessingChainJob> jobs;
	
	public ProcessingChainJobManager() {
		this.jobs = new HashMap<Integer, ProcessingChainJob>();
	}
	
	public ProcessingChainJob getProcessingChainJob(ProcessingChain chain) {
		Integer processingChainId = chain.getId();
		ProcessingChainJob job = jobs.get(processingChainId);
		if ( job == null ) {
			Workspace workspace = chain.getWorkspace();
			Context context = contextManager.getContext(workspace);
			// add chain-level parameters?
			job = new ProcessingChainJob(context);
			jobs.put(processingChainId, job);
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

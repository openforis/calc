package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author G. Miceli
 *
 */
public class ProcessingChainJobManager {
	
	@Autowired
	private ContextManager contextManager;

	@Autowired
	private ProcessingChainDao processingChainDao;
	
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
			job = Task.createTask(ProcessingChainJob.class, context, chain.parameters());
			jobs.put(processingChainId, job);
		}
		return job;
	}
}

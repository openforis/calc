package org.openforis.calc.engine;

import java.util.Set;
import java.util.UUID;

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
	private ProcessingChainJobManager processingChainJobManager;
	
	@Autowired 
	private TaskManager taskManager;

	@Transactional
	public void saveProcessingChain(ProcessingChain chain) {
		processingChainDao.save(chain);
		// TODO update Workspace?
	}

	synchronized	
	public ProcessingChainJob getProcessingChainJob(int chainId) throws InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(chainId);
		if ( chain == null ) {
			throw new IllegalArgumentException("No processing chain with id "+chainId);
		}
		return processingChainJobManager.getProcessingChainJob(chain);
	}
	
	synchronized
	public void startProcessingChainJob(int chainId, Set<UUID> taskIds) throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChainJob job = getProcessingChainJob(chainId);
		job.setScheduledTasks(taskIds);
		taskManager.start(job);
	}
}

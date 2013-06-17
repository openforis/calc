package org.openforis.calc.engine;

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
	
	@Transactional
	public void saveProcessingChain(ProcessingChain chain) {
		processingChainDao.save(chain);
		// TODO update Workspace?
	}
	
	public ProcessingChainJob getProcessingChainJob(int workspaceId) {
		return processingChainJobManager.getProcessingChainJob(workspaceId);
	}
}

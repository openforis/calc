package org.openforis.calc.engine;

import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainDao;
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
	
	@Transactional
	public void saveProcessingChain(ProcessingChain chain) {
		processingChainDao.saveWorkspace(chain);
		// TODO update Workspace?
	}
	
	public void createDefaultProcessingChain(Workspace ws) {
		ProcessingChain chain = new ProcessingChain();
		chain.setCaption(Workspace.DEFAULT_CHAIN_CAPTION);
		ws.addProcessingChain(chain);
		
		processingChainDao.saveWorkspace(chain);
	}
	
}

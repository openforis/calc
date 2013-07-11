package org.openforis.calc.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link ProcessingChain} instances.
 * 
 * @author M. Togna
 */
@Service
public class ProcessingChainManager {

	@Autowired
	private ProcessingChainDao dao;

	public ProcessingChainManager() {
	}

	@Transactional
	public ProcessingChain get(int chainId) {
		return dao.find(chainId);
	}

	@Transactional
	public ProcessingChain save(ProcessingChain chain) {
		return dao.save(chain);
	}

	// public List<ProcessingChain> loadAll(){
	// return dao.loadAll();
	// }
}

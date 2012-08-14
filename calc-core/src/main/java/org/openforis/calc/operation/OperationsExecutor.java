/**
 * 
 */
package org.openforis.calc.operation;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.OperationsException;

/**
 * @author Mino Togna
 * 
 */
public class OperationsExecutor {

	private static OperationsExecutor INSTANCE;

	private ExecutorService executorService;
	private List<Operation> defaultOperations;
	private boolean running;

	private OperationsExecutor() {
		executorService = Executors.newSingleThreadExecutor();
	}

	public static OperationsExecutor getInstance() {
		if ( INSTANCE == null ) {
			INSTANCE = new OperationsExecutor();
		}
		return INSTANCE;
	}

	/**
	 * Execute default Operations
	 * 
	 * @throws OperationsException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public synchronized void execute() throws OperationException, InterruptedException, ExecutionException {
		if ( defaultOperations == null || defaultOperations.size() == 0 ) {
			throw new OperationException("Default operations can't be null");
		}
		OperationChain chain = new OperationChain();
		chain.addAll(defaultOperations);
		this.execute(chain);
	}

	private synchronized void execute(OperationChain operationChain) throws OperationException, InterruptedException, ExecutionException {
		if ( !isRunning() ) {
			try {
				executorService.submit(operationChain).get();
			} catch ( Exception e ) {				
				executorService.shutdown();
				throw new RuntimeException("Error while executing calculations" , e);
			}
		} else {
			throw new OperationException("OperationsExecutor is already running");
		}
	}

	public boolean isRunning() {
		return running;
	}

	public List<Operation> getDefaultOperations() {
		return defaultOperations;
	}

	public void setDefaultOperations(List<Operation> defaultOperations) {
		this.defaultOperations = defaultOperations;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		executorService.shutdownNow();
	}
	
}

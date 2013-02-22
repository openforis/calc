/**
 * 
 */
package org.openforis.calc.operation;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.OperationsException;

/**
 * @author M. Togna
 * 
 */
public class OperationsExecutor extends Operation {

	private ExecutorService executorService;
	private List<Operation> defaultOperations;

	private OperationChain operationChain;
	private Future<Void> result;

	public OperationsExecutor() {
		super();
		executorService = Executors.newSingleThreadExecutor();
		operationChain = null;
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
		OperationChain chain = new OperationChain(defaultOperations);
		this.operationChain = chain;
		evaluate();
	}

	// public synchronized void execute(OperationChain operationChain) throws OperationException, InterruptedException, ExecutionException {
	// this.operationChain = operationChain;
	// evaluate();
	// }

	public boolean isRunning() {
		return operationChain != null && operationChain.isActive();
	}

	public List<Operation> getOperations() {
		return getDefaultOperations();
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
		shutdownNow();
	}

	@Override
	void evaluate() throws OperationException {
		if ( !isRunning() ) {
			// setStatus(Status.START);
			try {
				result = executorService.submit(operationChain);
			} catch ( Exception e ) {
				shutdownNow();
				throw new RuntimeException("Error while executing calculations", e);
			}
			// setStatus(Status.END);
		} else {
			throw new OperationException("OperationsExecutor is already running");
		}

	}
	
	public int completedOperations(){
		int i = 0;
		if(getOperations() != null){
			for ( Operation o : getOperations() ) {
				if(o.getStatus() == Status.END){
					i++;
				}
			}
		}
		return i;
	}

	private void shutdownNow() {
		executorService.shutdownNow();
		result = null;
	}

	@Override
	public Status getStatus() {
		if ( result == null ) {
			return Status.IDLE;
		} else if ( result.isDone() ) {
			// TODO check for exception
			return Status.END;
		} else if ( result.isCancelled() ) {
			return Status.INTERRUPT;
		} else {
			return Status.IDLE;
		}
	}
	// public void run() {
	// try {
	// this.execute();
	// } catch ( Exception e ) {
	// throw new RuntimeException("Error", e);
	// }
	// }

}

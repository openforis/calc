/**
 * 
 */
package org.openforis.calc.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.openforis.calc.operation.Operation.Status;

/**
 * @author Mino Togna
 * 
 */
class OperationChain implements Callable<Void> {

	private List<Operation> operations;

	private Operation currentOperation;

	OperationChain() {
		this(new ArrayList<Operation>());
	}

	OperationChain(List<Operation> operations) {
		this.operations = operations;
		this.currentOperation = null;
	}

	OperationChain addOperation(Operation operation) {
		if ( !operations.contains(operation) ) {
			operations.add(operation);
		}
		return this;
	}

	public Void call() throws Exception {
		resetOperationsStatus();

		for ( Operation operation : operations ) {
			try {
				currentOperation = operation;
				
				operation.setStatus(Status.START);
				operation.evaluate();
				operation.setStatus(Status.END);
				
			} catch ( OperationException e ) {
				operation.setStatus(Status.INTERRUPT);
				throw new RuntimeException("Error in evaluvating operation ", e);
			}
		}
		currentOperation = null;
		return null;
	}

	private void resetOperationsStatus() {
		for ( Operation operation : operations ) {
			operation.setStatus(Status.IDLE);
		}
	}

	public boolean isActive(){
		return getCurrentOperation() != null;
	}
	
	Operation getCurrentOperation() {
		return currentOperation;
	}

	void addAll(List<Operation> operations) {
		this.operations.addAll(operations);
	}

}

/**
 * 
 */
package org.openforis.calc.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Mino Togna
 * 
 */
class OperationChain implements Callable<Void> {

	private List<Operation> operations;

	private Operation currentOperation;

	OperationChain() {
		this.operations = new ArrayList<Operation>();
		this.currentOperation = null;
	}

	OperationChain addOperation(Operation operation) {
		if ( !operations.contains(operation) ) {
			operations.add(operation);
		}
		return this;
	}

	public Void call() throws Exception {
		for ( Operation operation : operations ) {
			try {
				currentOperation = operation;
				operation.evaluate();
			} catch ( OperationException e ) {
				throw new RuntimeException("Error in evaluvating operation ", e);
			}
		}
		currentOperation = null;
		return null;
	}

	Operation getCurrentOperation() {
		return currentOperation;
	}

	void addAll(List<Operation> operations) {
		this.operations.addAll(operations);
	}

}

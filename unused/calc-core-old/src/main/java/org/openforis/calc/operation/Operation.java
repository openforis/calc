/**
 * 
 */
package org.openforis.calc.operation;

/**
 * @author M. Togna
 * 
 */
public abstract class Operation {

	abstract void evaluate() throws OperationException;

	private Status status;

	public Operation() {
		status = Status.IDLE;
	}

	public enum Status {
		START, INTERRUPT, END, IDLE;
	}

	/**
	 * Return the name of the operation By default it returns the class simple name
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public Status getStatus() {
		return status;
	}

	void setStatus(Status status) {
		this.status = status;
	}

}

package org.openforis.calc.chain;

/**
 * 
 * @author G. Miceli
 *
 */
public class InvalidProcessingChainException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidProcessingChainException() {
		super();
	}

	public InvalidProcessingChainException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidProcessingChainException(String message) {
		super(message);
	}

	public InvalidProcessingChainException(Throwable cause) {
		super(cause);
	}
}

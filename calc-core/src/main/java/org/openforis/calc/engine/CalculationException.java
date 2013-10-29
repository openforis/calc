/**
 * 
 */
package org.openforis.calc.engine;

/**
 * @author M. Togna
 *
 */
public class CalculationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
//	public CalculationException() {
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @param message
	 */
	public CalculationException(String message) {
		super(message);
	}

//	/**
//	 * @param cause
//	 */
//	public CalculationException(Throwable cause) {
//		super(cause);
//	}

	/**
	 * @param message
	 * @param cause
	 */
	public CalculationException(String message, Throwable cause) {
		super(message, cause);
	}

}

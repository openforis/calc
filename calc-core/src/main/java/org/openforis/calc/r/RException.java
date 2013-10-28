package org.openforis.calc.r;

/***
 * 
 * @author G. Miceli
 *
 */
public class RException extends Exception {

	private static final long serialVersionUID = 1L;

	public RException() {
		super();
	}

	public RException(String message, Throwable cause) {
		super(message, cause);
	}

	public RException(String message) {
		super(message);
	}

	public RException(Throwable cause) {
		super(cause);
	}

}

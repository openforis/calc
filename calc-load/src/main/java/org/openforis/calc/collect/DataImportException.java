package org.openforis.calc.collect;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataImportException extends Exception {

	private static final long serialVersionUID = 1L;

	protected DataImportException() {
		super();
	}

	protected DataImportException(String message, Throwable cause) {
		super(message, cause);
	}

	protected DataImportException(String message) {
		super(message);
	}

	protected DataImportException(Throwable cause) {
		super(cause);
	}
}

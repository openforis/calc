package org.openforis.calc.dataimport.collect;

/**
 * 
 * @author G. Miceli
 *
 */
public class InvalidMetadataException extends Exception {

	private static final long serialVersionUID = 1L;

	protected InvalidMetadataException() {
		super();
	}

	protected InvalidMetadataException(String message, Throwable cause) {
		super(message, cause);
	}

	protected InvalidMetadataException(String message) {
		super(message);
	}

	protected InvalidMetadataException(Throwable cause) {
		super(cause);
	}
}

package org.openforis.calc.importer.collect;

/**
 * 
 * @author G. Miceli
 *
 */
public class InvalidMetadataException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidMetadataException() {
		super();
	}

	public InvalidMetadataException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidMetadataException(String message) {
		super(message);
	}

	public InvalidMetadataException(Throwable cause) {
		super(cause);
	}
}

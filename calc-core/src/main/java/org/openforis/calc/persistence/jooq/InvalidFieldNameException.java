package org.openforis.calc.persistence.jooq;


/**
 * @author G. Miceli
 */
public class InvalidFieldNameException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public InvalidFieldNameException() {
	}

	public InvalidFieldNameException(String s) {
		super(s);
	}
	
    static InvalidFieldNameException forFieldName(String s) {
        return new InvalidFieldNameException("For name: \"" + s + "\"");
    }
}

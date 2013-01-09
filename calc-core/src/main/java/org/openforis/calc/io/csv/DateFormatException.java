package org.openforis.calc.io.csv;

/**
 * @author G. Miceli
 */
public class DateFormatException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public DateFormatException() {
	}

	public DateFormatException(String s) {
		super(s);
	}
	
    static DateFormatException forInputString(String s) {
        return new DateFormatException("For input string: \"" + s + "\"");
    }
}

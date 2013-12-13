/**
 * 
 */
package org.openforis.calc.r;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 * 
 */
public class RLogger {
	private static final String CALC_ERROR_SIGNAL = "CALC-ERROR";

	private List<RLoggerLine> lines;
	@JsonIgnore
	private boolean containsCalcErrorSignal;

	public RLogger() {
		this.lines = new ArrayList<RLogger.RLoggerLine>();
		this.containsCalcErrorSignal = false;
	}

	public void append(int oType, String text) {
		RLoggerLine line = new RLoggerLine(oType, text);
		lines.add(line);

		// check if there has been an error
		if ( CALC_ERROR_SIGNAL.equals(text.trim()) ) {
			this.containsCalcErrorSignal = true;
		}
	}

	public boolean containsCalcErrorSignal() {
		return containsCalcErrorSignal;
	}

	static class RLoggerLine {

		private int oType;
		private String text;

		/**
		 * 
		 * @param oType
		 *            can be 0 (no error) or 1 (error/warning). see r
		 *            documentation
		 * @param text
		 */
		RLoggerLine(int oType, String text) {
			this.oType = oType;
			this.text = text;
		}

		public int getoType() {
			return oType;
		}

		public String getText() {
			return text;
		}
	}

}

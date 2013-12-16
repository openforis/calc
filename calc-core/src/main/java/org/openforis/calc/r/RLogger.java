/**
 * 
 */
package org.openforis.calc.r;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Mino Togna
 * 
 */
public class RLogger {
	private static final Pattern LINE_PATTERN = Pattern.compile( "\n*(.+)\n*" );

	// if you change this, make sure it has been changed into
	// org/openforis/calc/r/functions.R as well
	private static final String CALC_ERROR_SIGNAL = "CALC-ERROR";

	private List<RLoggerLine> lines;

	@JsonIgnore
	private boolean containsCalcErrorSignal;

	private RLoggerLine tempLine;
	
	
	public RLogger() {
		this.lines = new ArrayList<RLogger.RLoggerLine>();
		this.containsCalcErrorSignal = false;
	}

	
	public void append(int oType, String text) {
		if ( StringUtils.isNotEmpty(text) && !text.equals("[1]") ) {
			// check if there has been an error
			if (CALC_ERROR_SIGNAL.equals(text.trim())) {
				this.containsCalcErrorSignal = true;
			} else {
				getTempLine(oType).append( text );
			}
		}
	}
	
	void flush() {
		if( this.tempLine != null ) {
			String text = this.tempLine.getText();
			// find new lines to add 
			Matcher matcher = LINE_PATTERN.matcher(text);
			while( matcher.find() ){
				String lineText = matcher.group(1);
				if(lineText.contains("\\")){
					System.out.println("aaaa");
				}
				RLoggerLine line = new RLoggerLine(this.tempLine.oType, lineText);
				this.lines.add(line);
			}
			this.tempLine = null;
		}
	}
	
	private RLoggerLine getTempLine(int oType) {
		if( this.tempLine == null ) {
			this.tempLine = new RLoggerLine(oType);
		}
		return tempLine;
	}
	
	public boolean containsCalcErrorSignal() {
		return containsCalcErrorSignal;
	}

	public List<RLoggerLine> getLines() {
		return lines;
	}

	static class RLoggerLine {

		private int oType;
		private StringBuilder sb ;
		/**
		 * 
		 * @param oType
		 *            can be 0 (no error) or 1 (error/warning). see r
		 *            documentation
		 * @param text
		 */
		RLoggerLine(int oType, String text) {
			this.oType = oType;
			this.sb = new StringBuilder();
			if( text != null ){
				sb.append( text );
			}
		}

		void append(String text) {
			this.sb.append( text );
		}

		RLoggerLine(int oType) {
			this(oType, null);
		}
		
		public int getoType() {
			return oType;
		}
		
		@JsonInclude
		public String getText() {
			return this.sb.toString();
		}
	}

}

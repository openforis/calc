package org.openforis.calc.util.csv;
import java.util.Map;


public class CsvLine {
	private Map<String, Integer> columns;
	private String[] line;

	CsvLine(Map<String, Integer> columns, String[] line) {
		this.columns = columns;
		this.line = line;
	}
	
	public String[] getLine() {
		return line;
	}
	
	public String getString(int idx) {
		return line[idx];
	}
	
	public String getString(String column) {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return line[idx];
		}
	}

	public Integer getInteger(int idx) {
		return toInteger(line[idx]);
	}

	public Integer getInteger(String column) {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toInteger(line[idx]);
		}
	}

	public Double getDouble(int idx) {
		return toDouble(line[idx]);
	}

	public Double getDouble(String column) {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toDouble(line[idx]);
		}
	}

	public Boolean getBoolean(int idx) {
		return toBoolean(line[idx]);
	}

	public Boolean getBoolean(String column) {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toBoolean(line[idx]);
		}
	}
	private Integer toInteger(String val) {
		return val == null || val.isEmpty() ? null : Integer.valueOf(val);
	}

	private Double toDouble(String val) {
		return val == null || val.isEmpty() ? null : Double.valueOf(val);
	}

	private Boolean toBoolean(String val) {
		if ( val == null ) {
			return null;
		} else if ( val.equals("1") || 
					val.equalsIgnoreCase("T") || 
					val.equalsIgnoreCase("Y") || 
					val.equalsIgnoreCase("true") ){
			return true;
		} else if ( val.equals("0") || 
					val.equalsIgnoreCase("F") || 
					val.equalsIgnoreCase("N") || 
					val.equalsIgnoreCase("false") ){
			return false;
		} else {
			throw new NumberFormatException("'"+val+"' is not a valid boolean value");
		}
	}

	public Integer getColumnIndex(String column) {
		if ( column == null ) {
			throw new IllegalStateException("Column headers not yet read");
		}
		return columns.get(column);
	}
}
 
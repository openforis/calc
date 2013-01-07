package org.openforis.calc.util.csv;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class CsvLine {
	private Map<String, Integer> columns;
	private String[] line;
	private CsvReader reader;
	
	CsvLine(CsvReader reader, String[] line) {
		this.columns = reader.getColumnIndices();
		this.reader = reader;
		this.line = line;
	}
	
	public String[] getLine() {
		return line;
	}
	
	public String getString(int idx) {
		return toString(line[idx]);
	}
	
	public String getString(String column) {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toString(line[idx]);
		}
	}

	private String toString(String txt) {
		if ( txt == null || txt.trim().isEmpty() || "NA".equals(txt) ) { 		
			return null;
		} else {
			return txt;
		}
	}

	public Integer getInteger(int idx) throws NumberFormatException {
		return toInteger(line[idx]);
	}

	public Integer getInteger(String column) throws NumberFormatException {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toInteger(line[idx]);
		}
	}

	public Double getDouble(int idx) throws NumberFormatException {
		return toDouble(line[idx]);
	}

	public Double getDouble(String column) throws NumberFormatException {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toDouble(line[idx]);
		}
	}

	public Date getDate(int idx) throws ParseException {
		return toDate(line[idx]);
	}

	public Date getDate(String column) throws ParseException {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toDate(line[idx]);
		}
	}

	public Boolean getBoolean(int idx) throws NumberFormatException {
		return toBoolean(line[idx]);
	}

	public Boolean getBoolean(String column) throws NumberFormatException {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return toBoolean(line[idx]);
		}
	}
	
	private Integer toInteger(String val) {
		return isNullValue(val) ? null : Double.valueOf(val).intValue();
	}

	private Double toDouble(String val) {
		return isNullValue(val) ? null : Double.valueOf(val);
	}
	
	private boolean isNullValue(String val) {
		return val == null || val.isEmpty() || "NA".equals(val);
	}

	private Boolean toBoolean(String val) {
		if ( isNullValue(val) ) {
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

	private Date toDate(String val) throws ParseException {
		return isNullValue(val) ? null : reader.getDateFormat().parse(val);
	}
	
	public CsvReader getReader() {
		return reader;
	}

	public List<String> getColumnNames() {
		return Collections.unmodifiableList(new ArrayList<String>(columns.keySet()));
	}
}
 
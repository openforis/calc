package org.openforis.calc.io.csv;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public class CsvLine implements Record {
	private Map<String, Integer> columns;
	private String[] line;
	private CsvReader csvReader;
	
	CsvLine(CsvReader csvReader, String[] line) {
		this.columns = csvReader.getColumnIndices();
		this.csvReader = csvReader;
		this.line = line;
	}
	
	public String[] getLine() {
		return line;
	}
	
	public String getString(int idx) {
		return toString(line[idx]);
	}
	
	@Override
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
	
	@Override
	public Integer getInteger(int idx) throws NumberFormatException {
		return toInteger(line[idx]);
	}

	@Override
	public Integer getInteger(String column) throws NumberFormatException {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return getInteger(idx);
		}
	}

	@Override
	public Double getDouble(int idx) throws NumberFormatException {
		return toDouble(line[idx]);
	}

	@Override
	public Double getDouble(String column) throws NumberFormatException {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return getDouble(idx);
		}
	}

	@Override
	public Date getDate(int idx) throws DateFormatException {
		try {
			return toDate(line[idx]);
		} catch (ParseException e) {
			throw DateFormatException.forInputString(line[idx]);
		}
	}

	@Override
	public Date getDate(String column) throws DateFormatException {
		Integer idx = getColumnIndex(column);
		return getDate(idx);
	}

	@Override
	public Boolean getBoolean(int idx) throws NumberFormatException {
		return toBoolean(line[idx]);
	}

	@Override
	public Boolean getBoolean(String column) throws NumberFormatException {
		Integer idx = getColumnIndex(column);
		if ( idx == null ) {
			return null;
		} else {
			return getBoolean(idx);
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
		return isNullValue(val) ? null : csvReader.getDateFormat().parse(val);
	}

	public List<String> getColumnNames() {
		return Collections.unmodifiableList(new ArrayList<String>(columns.keySet()));
	}

	@Override
	public String[] toStringArray() {
		return line;
	}

	@Override
	public FlatDataStream getFlatDataStream() {		
		return csvReader;
	}

	@Override
	public List<String> getFieldNames() {
		return csvReader.getColumnNames();
	}

}
 
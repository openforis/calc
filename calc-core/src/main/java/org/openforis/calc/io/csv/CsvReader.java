package org.openforis.calc.io.csv;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.io.FlatDataStream;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 * @author G. Miceli
 *
 */
public class CsvReader implements FlatDataStream {

	private Map<String, Integer> columns;
	private DateFormat dateFormat;
	private CSVReader csv;
	
	public CsvReader(Reader reader) {
		csv = new CSVReader(reader);
	}

	public void readHeaderLine() throws IOException {
		String[] headers = csv.readNext();
		columns = new HashMap<String, Integer>();
		for (int i = 0; i < headers.length; i++) {
			columns.put(headers[i], i);
		}
	}
	
	public CsvLine readNextLine() throws IOException {
		String[] line = csv.readNext();
		if ( line == null ) {
			return null;
		} else {
			return new CsvLine(this, line);
		}
	}
	
	Map<String, Integer> getColumnIndices() {
		return Collections.unmodifiableMap(columns);
	}
	
	public DateFormat getDateFormat() {
		if ( dateFormat == null ) {
			setDateFormat("yyyy-MM-dd");
		}
		return dateFormat;
	}
	
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	public void setDateFormat(String pattern) {
		this.dateFormat = new SimpleDateFormat(pattern);
	}
	
	public List<String> getColumnNames() {
		return Collections.unmodifiableList(new ArrayList<String>(columns.keySet()));
	}

	public void close() throws IOException {
		csv.close();
	}
}

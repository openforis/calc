package org.openforis.calc.util.csv;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;


public class CsvReader extends CSVReader {

	private Map<String, Integer> columns;
	
	public CsvReader(Reader reader) {
		super(reader);
	}

	public void readHeaderLine() throws IOException {
		String[] headers = readNext();
		columns = new HashMap<String, Integer>();
		for (int i = 0; i < headers.length; i++) {
			columns.put(headers[i], i);
		}
	}
	
	public CsvLine readNextLine() throws IOException {
		String[] line = readNext();
		if ( line == null ) {
			return null;
		} else {
			return new CsvLine(columns, line);
		}
	}
}

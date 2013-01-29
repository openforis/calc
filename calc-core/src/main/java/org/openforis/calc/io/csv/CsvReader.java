package org.openforis.calc.io.csv;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 * @author G. Miceli
 *
 */
public class CsvReader extends CsvProcessor implements FlatDataStream {
	
	private static final String EOF = "EOF"; 
	
	private CSVReader csv;
	private long linesRead;
	private boolean headersRead;
	
	public CsvReader(String filename) throws FileNotFoundException {
		this(new FileReader(filename));
	}
	
	public CsvReader(Reader reader) {
		csv = new CSVReader(reader);
		headersRead = false;
		linesRead = 0;
	}

	public void readHeaders() throws IOException {
		if ( headersRead ) {
			throw new IllegalStateException("Headers already read");
		}
		String[] headers = csv.readNext();
		setColumnNames(headers);
		headersRead = true;
	}

	public CsvLine readNextLine() throws IOException {
		if ( !headersRead ) {
			throw new IllegalStateException("Headers must be read first");
		}
		
		String[] line = csv.readNext();
		if( isEndOfStream( line ) ) {
			return null;
		} else {
			return new CsvLine(this, line);
		}
	}
	
	private boolean isEndOfStream(String[] line) {
		return ( line == null || line.length == 0 || ( line.length == 1 && EOF.equals(line[0]) ) );
	}

	public void close() throws IOException {
		csv.close();
	}

	public boolean isHeadersRead() {
		return headersRead;
	}

	public long getLinesRead() {
		return linesRead;
	}
	
	@Override
	public List<String> getFieldNames() {
		return getColumnNames();
	}

	@Override
	public FlatRecord nextRecord() throws IOException {
		return readNextLine();
	}
}

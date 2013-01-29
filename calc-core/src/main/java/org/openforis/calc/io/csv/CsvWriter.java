package org.openforis.calc.io.csv;

import static au.com.bytecode.opencsv.CSVWriter.NO_QUOTE_CHARACTER;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;

import au.com.bytecode.opencsv.CSVWriter;
/**
 * @author G. Miceli
 */
public class CsvWriter extends CsvProcessor {
	private CSVWriter csvWriter;
	private long linesWritten; 
	private boolean headersWritten;
	
	public CsvWriter(Writer writer) {
		csvWriter = new CSVWriter(writer, ',', NO_QUOTE_CHARACTER);
		linesWritten = 0;
		headersWritten = false;
	}
	
	public CsvWriter(OutputStream out) {
		this(new BufferedWriter(new OutputStreamWriter(out)));
	}

	public void writeAll(FlatDataStream in) throws IOException {
		FlatRecord r = in.nextRecord();
		if ( r == null ) {
			return;
		}
    	List<String> fieldNames = in.getFieldNames();
    	String[] headers = fieldNames.toArray(new String[fieldNames.size()]);
    	writeHeaders(headers);
		
		while ( r != null ) {
			writeNext(r);
			r = in.nextRecord();
		}
	}

	public void close() throws IOException {
		csvWriter.close();
	}
	
	public void writeNext(FlatRecord r) {
		String[] line = r.toStringArray();
		csvWriter.writeNext(line);
		linesWritten++;
	}

	public void writeHeaders(String[] headers) {
		if ( headersWritten ) {
			throw new IllegalStateException("Headers already written");
		}
		setColumnNames(headers);
    	csvWriter.writeNext(headers);
    	headersWritten = true;
	}

	public long getLinesWritten() {
		return linesWritten;
	}
	
	public boolean isHeadersWritten() {
		return headersWritten;
	}
}

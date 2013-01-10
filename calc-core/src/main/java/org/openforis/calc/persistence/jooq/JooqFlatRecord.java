package org.openforis.calc.persistence.jooq;

import java.util.List;

import org.jooq.Record;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;

/**
 * @author G. Miceli
 * 
 */
class JooqFlatRecord implements FlatRecord {
	private Record jooqRecord;
	private FlatDataStream dataStream;

	JooqFlatRecord(Record record, FlatDataStream dataStream) {
		this.jooqRecord = record;
		this.dataStream = dataStream;
	}
	@Override
	public String[] toStringArray() {
		String[] s = new String[jooqRecord.size()];
		for ( int i = 0 ; i < jooqRecord.size() ; i++ ) {
			s[i] = jooqRecord.getValue(i, String.class);
		}
		return s;
	}

	@Override 
	public FlatDataStream getFlatDataStream() {
		return dataStream;
	}

	@Override
	public List<String> getFieldNames() {
		return dataStream.getFieldNames();
	}

	@Override
	public <T> T getValue(int idx, Class<T> type) {
		return jooqRecord.getValue(idx, type);
	}

	@Override
	public <T> T getValue(String name, Class<T> type) {
		return jooqRecord.getValue(name, type);
	}
	@Override
	public boolean isMissing(int idx) {
		return jooqRecord.getValue(idx) == null;
	}
	@Override
	public boolean isMissing(String name) {
		return jooqRecord.getValue(name) == null;
	}
	
}

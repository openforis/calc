package org.openforis.calc.persistence.jooq;

import java.util.Date;
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
	public Integer getInteger(String name) {
		return jooqRecord.getValue(name, Integer.class);
	}

	@Override
	public Double getDouble(String name) {
		return jooqRecord.getValue(name, Double.class);
	}

	@Override
	public Boolean getBoolean(String name) {
		return jooqRecord.getValue(name, Boolean.class);
	}

	@Override
	public String getString(String name) {
		return jooqRecord.getValue(name, String.class);
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
	public Date getDate(String name) {
		return jooqRecord.getValue(name, Date.class);
	}

	@Override
	public Integer getInteger(int idx) {
		return jooqRecord.getValue(idx, Integer.class);
	}

	@Override
	public Double getDouble(int idx) {
		return jooqRecord.getValue(idx, Double.class);
	}

	@Override
	public Boolean getBoolean(int idx) {
		return jooqRecord.getValue(idx, Boolean.class);
	}

	@Override
	public Date getDate(int idx) {
		return jooqRecord.getValue(idx, Date.class);
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
	
}

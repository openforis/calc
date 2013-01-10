package org.openforis.calc.persistence.jooq;

import java.util.Date;
import java.util.List;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.Record;

/**
 * @author G. Miceli
 * 
 */
class JooqRecord implements Record {
	private org.jooq.Record record;
	private FlatDataStream dataStream;

	JooqRecord(org.jooq.Record record, FlatDataStream dataStream) {
		this.record = record;
		this.dataStream = dataStream;
	}

	@Override
	public Integer getInteger(String name) {
		return record.getValueAsInteger(name);
	}

	@Override
	public Double getDouble(String name) {
		return record.getValueAsDouble(name);
	}

	@Override
	public Boolean getBoolean(String name) {
		return record.getValueAsBoolean(name);
	}

	@Override
	public String getString(String name) {
		return record.getValueAsString(name);
	}

	@Override
	public String[] toStringArray() {
		String[] s = new String[record.size()];
		for ( int i = 0 ; i < record.size() ; i++ ) {
			s[i] = record.getValueAsString(i);
		}
		return s;
	}

	@Override
	public Date getDate(String name) {
		return record.getValueAsDate(name);
	}

	@Override
	public Integer getInteger(int idx) {
		return record.getValueAsInteger(idx);
	}

	@Override
	public Double getDouble(int idx) {
		return record.getValueAsDouble(idx);
	}

	@Override
	public Boolean getBoolean(int idx) {
		return record.getValueAsBoolean(idx);
	}

	@Override
	public Date getDate(int idx) {
		return record.getValueAsDate(idx);
	}

	@Override
	public FlatDataStream getFlatDataStream() {
		return dataStream;
	}

	@Override
	public List<String> getFieldNames() {
		return dataStream.getFieldNames();
	}

}

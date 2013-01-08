package org.openforis.calc.persistence.jooq;

import org.openforis.calc.io.flat.Record;

/**
 * @author G. Miceli
 *
 */
class JooqRecord implements Record {
	private org.jooq.Record record;
	
	JooqRecord(org.jooq.Record record) {
		this.record = record;
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
		for (int i = 0; i < record.size(); i++) {
			s[i] = record.getValueAsString(i);
		}
		return s;
	}
}

package org.openforis.calc.persistence.jooq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jooq.Field;
import org.jooq.Result;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.Record;

/**
 * @author G. Miceli
 */
public class JooqResultDataStream implements FlatDataStream {

	private List<String> fieldNames;
	private Iterator<? extends org.jooq.Record> jooqResultIterator;
	
	public JooqResultDataStream(Result<? extends org.jooq.Record> jooqResult) {
		setFieldNames(jooqResult.getFields());
		this.jooqResultIterator = jooqResult.iterator(); 
	}
	
	public JooqResultDataStream(org.jooq.Record jooqRecord) {
		setFieldNames(jooqRecord.getFields());
		this.jooqResultIterator = Arrays.asList(jooqRecord).iterator(); 
	}

	@Override
	public List<String> getFieldNames() {
		return fieldNames;
	}
	
	private void setFieldNames(List<Field<?>> fields) {
		List<String> colnames = new ArrayList<String>(fields.size());
		for (Field<?> field : fields) {
			colnames.add(field.getName());
		}
		this.fieldNames = Collections.unmodifiableList(colnames);
	}
	
	public Record nextRecord() {
		if ( jooqResultIterator.hasNext() ) {
			org.jooq.Record r = jooqResultIterator.next();
			return new JooqRecord(r, this);
		} else { 
			return null;
		}
	}
}

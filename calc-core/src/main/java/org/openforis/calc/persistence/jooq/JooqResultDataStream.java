package org.openforis.calc.persistence.jooq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jooq.Field;
import org.jooq.Result;
import org.openforis.calc.io.FlatDataStream;
import org.openforis.calc.io.flat.Record;

/**
 * @author G. Miceli
 */
public class JooqResultDataStream implements FlatDataStream {

	private List<String> fieldNames;
	private Iterator<? extends org.jooq.Record> jooqResultIterator;
	
	public JooqResultDataStream(Result<? extends org.jooq.Record> jooqResult) {
		this.fieldNames = getFieldNames(jooqResult);
		this.jooqResultIterator = jooqResult.iterator(); 
	}
	
	@Override
	public List<String> getFieldNames() {
		return fieldNames;
	}
	
	private static List<String> getFieldNames(Result<?> jooqResult) {
		List<Field<?>> fields = jooqResult.getFields();
		List<String> colnames = new ArrayList<String>(fields.size());
		for (Field<?> field : fields) {
			colnames.add(field.getName());
		}
		return Collections.unmodifiableList(colnames);
	}
	
	public Record nextRecord() {
		if ( jooqResultIterator.hasNext() ) {
			org.jooq.Record r = jooqResultIterator.next();
			return new JooqRecord(r);
		} else { 
			return null;
		}
	}
}

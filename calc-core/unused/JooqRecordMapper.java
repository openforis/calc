package org.openforis.calc.persistence.jooq;

import org.openforis.calc.io.flat.Record;
import org.openforis.calc.io.flat.RecordMapper;

/**
 * 
 * @author G. Miceli
 *
 */
public class JooqRecordMapper<A extends Record<A>, B extends org.jooq.Record> implements RecordMapper<A, JooqRecord<B>> {

	@Override
	public void map(A sourceRecord, JooqRecord<B> targetRecord) {
		// TODO Auto-generated method stub
		
	}

}

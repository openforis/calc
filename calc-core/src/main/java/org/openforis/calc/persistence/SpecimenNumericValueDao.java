package org.openforis.calc.persistence;

import static org.jooq.impl.Factory.row;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenNumericValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component 
@Transactional
public class SpecimenNumericValueDao extends JooqDaoSupport<SpecimenNumericValueRecord, SpecimenNumericValue> {

	private org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue S = org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue.SPECIMEN_NUMERIC_VALUE;
	private org.openforis.calc.persistence.jooq.tables.TmpNumericValue TNV = org.openforis.calc.persistence.jooq.tables.TmpNumericValue.TMP_NUMERIC_VALUE; 
	
	public SpecimenNumericValueDao() {
		super(Tables.SPECIMEN_NUMERIC_VALUE, SpecimenNumericValue.class);
	}
	
	@Transactional
	synchronized
	public void batchUpdate(int obsUnitId, FlatDataStream dataStream, VariableMetadata... variables) throws IOException {
		long start = System.currentTimeMillis();
		ArrayList<Query> queries = new ArrayList<Query>();
		
		Factory create = getJooqFactory();
		
		int transactionId = create.nextval( Sequences.TRANSACTION_ID_SEQ ).intValue();
		
		FlatRecord r = null;
		int cnt = 0;
		while ( (r = dataStream.nextRecord() ) != null ) {
			for( VariableMetadata varMetadata : variables ) {
				cnt++;
				
				Integer specimenId = r.getValue("specimen_id", Integer.class);
				Integer variableId = varMetadata.getVariableId();
				Double value = r.getValue( varMetadata.getVariableName(), Double.class );
				
				//1. insert into tmp table
				Query insert = getTempValueInsert(create, transactionId, specimenId, variableId, value);
				queries.add( insert );
				
				if( cnt % 1000 == 0 ) {
					create.batch( queries ).execute();
					queries = new ArrayList<Query>();
				}
			}
		}
		
		Query deleteNumericValues = getNumericValuesDelete(create, transactionId);
		queries.add( deleteNumericValues );
		
		Query insertNumericValues = getNumericValuesInsert(create, transactionId);
		queries.add( insertNumericValues );
		
		Query deleteTmpValues = getTempValuesDelete(create, transactionId);
		queries.add( deleteTmpValues );
		
		create.batch( queries ).execute();
		
		if( getLog().isDebugEnabled() ){
			long end = System.currentTimeMillis() - start;
			getLog().debug("===================== Saving " + Arrays.toString(variables) + " Executed in("+end+" mills): " + TimeUnit.MILLISECONDS.toSeconds(end)+" seconds");
		}
	}

	private Query getTempValueInsert(Factory create, int transactionId, Integer specimenId, Integer variableId, Double value) {
		return create
			.insertInto(TNV, TNV.TRANSACTION_ID, TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE)
			.values(transactionId, specimenId, variableId, value);
	}

	private Query getNumericValuesDelete(Factory create, int transactionId) {
		return create.delete( S )
				.where( 
						row(S.SPECIMEN_ID, S.VARIABLE_ID)
								.in( 
									create.select(TNV.OBJECT_ID,TNV.VARIABLE_ID)
									.from(TNV)
									.where( TNV.TRANSACTION_ID.eq( transactionId ) )
						)
				);
	}

	private Query getNumericValuesInsert(Factory create, int transactionId) {
		return create
			.insertInto( S, S.SPECIMEN_ID, S.VARIABLE_ID, S.VALUE, S.COMPUTED )
			.select( 
					create
						.select( TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE, Factory.value(true, Boolean.class) )
						.from( TNV )
						.where( TNV.TRANSACTION_ID.eq(transactionId).and( TNV.VALUE.isNotNull() ) )
					);
	}

	private Query getTempValuesDelete(Factory create, int transactionId) {
		return create.delete( TNV ).where( TNV.TRANSACTION_ID.eq(transactionId) );
	}
	
}
package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.Specimen;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenNumericValueRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.openforis.commons.io.flat.FlatRecord;
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
	public void updateCurrentValue(int obsUnitId, FlatDataStream dataStream, List<VariableMetadata> variables) throws IOException {
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
				Query insert = createTmpValuesInsertQuery(create, transactionId, specimenId, variableId, value);
				queries.add( insert );
				
				if( cnt % 2000 == 0 ) {
					executeQueries(queries);
				}
			}
		}
		
		Query delete = createCurrentValuesDeleteQuery(create, transactionId);
		queries.add(delete);

		Query update = createCurrentValuesUpdateQuery(create, transactionId, false);
		queries.add(update);

		Query insert = createCurrentValuesInsertQuery(create, transactionId);
		queries.add(insert);

		Query deleteTmp = createTmpValuesDeleteQuery(create, transactionId);
		queries.add(deleteTmp);

		executeQueries(queries);
		
		long end = System.currentTimeMillis() - start;
		getLog().debug("Updating specimen numerical values for variables "+variables.toString()+" executed in " + TimeUnit.MILLISECONDS.toSeconds(end)+" seconds");
	}

	@Transactional
	private void executeQueries(ArrayList<Query> queries) {
		Factory create = getJooqFactory();
		create.batch( queries ).execute();
		queries.clear();
	}

	private Query createTmpValuesInsertQuery(Factory create, int transactionId, Integer specimenId, Integer variableId, Double value) {
		return create
			.insertInto(TNV, TNV.TRANSACTION_ID, TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE)
			.values(transactionId, specimenId, variableId, value);
	}

	private Query createCurrentValuesDeleteQuery(Factory create, int transactionId) {
		return create.delete( S )
				.where( 
						S.ORIGINAL.isFalse()
						.and(
							Factory.row(S.SPECIMEN_ID, S.VARIABLE_ID)
									.in( 
										create.select(TNV.OBJECT_ID,TNV.VARIABLE_ID)
										.from(TNV)
										.where( TNV.TRANSACTION_ID.eq( transactionId )) )
						)
				);
	}
	
	private Query createCurrentValuesUpdateQuery(Factory create, int transactionId, boolean currentValue) {
		return 
				create
				.update( S )
				.set( S.CURRENT, currentValue )
				.where(
						Factory.row(S.SPECIMEN_ID, S.VARIABLE_ID)
						.in(
								create.select(TNV.OBJECT_ID, TNV.VARIABLE_ID)
								.from(TNV)
								.where( TNV.TRANSACTION_ID.eq( transactionId ) ) 
							)
					);
	}

	private Query createCurrentValuesInsertQuery(Factory create, int transactionId) {
		return create
			.insertInto( S, S.SPECIMEN_ID, S.VARIABLE_ID, S.VALUE, S.ORIGINAL, S.CURRENT )
			.select( 
					create
						.select( TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE, Factory.value(false, Boolean.class), Factory.value(true, Boolean.class) )
						.from( TNV )
						.where( TNV.TRANSACTION_ID.eq(transactionId) )
					);
	}

	private Query createTmpValuesDeleteQuery(Factory create, int transactionId) {
		return create.delete( TNV ).where( TNV.TRANSACTION_ID.eq(transactionId) );
	}
	
	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue v = SPECIMEN_NUMERIC_VALUE.as("v");
		Specimen o = SPECIMEN.as("o");
		create.delete(v)
			  .where(v.SPECIMEN_ID.in(
					  	create.select(o.SPECIMEN_ID)
					  		  .from(o)
					  		  .where(o.OBS_UNIT_ID.eq(id))));
	}
}
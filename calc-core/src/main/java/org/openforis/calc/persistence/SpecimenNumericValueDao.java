package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;

import org.jooq.impl.Factory;
import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.Specimen;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenNumericValueRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component 
@Transactional
public class SpecimenNumericValueDao extends JooqDaoSupport<SpecimenNumericValueRecord, SpecimenNumericValue> {
	
	@Autowired
	private SpecimenValueDao specimenValueDao;
	
	public SpecimenNumericValueDao() {
		super(Tables.SPECIMEN_NUMERIC_VALUE, SpecimenNumericValue.class);
	}
	
	@Transactional
	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue v = SPECIMEN_NUMERIC_VALUE;
		Specimen o = SPECIMEN.as("o");
		create.delete(v)
			  .where(v.SPECIMEN_ID.in(
					  	create.select(o.SPECIMEN_ID)
					  		  .from(o)
					  		  .where(o.OBS_UNIT_ID.eq(id))
			  		  	)
	  		  )
	  		  .execute();
	}
	
//	@Transactional
//	synchronized
//	public void updateCurrentValues(int obsUnitId, FlatDataStream dataStream, List<VariableMetadata> variables) throws IOException {
//		
//		specimenValueDao.updateCurrentValues(obsUnitId, dataStream, variables);
		
//		long start = System.currentTimeMillis();
//		ArrayList<Query> queries = new ArrayList<Query>();
//		
//		Factory create = getJooqFactory();
//		
//		int transactionId = create.nextval( Sequences.TRANSACTION_ID_SEQ ).intValue();
//		
//		FlatRecord r = null;
//		int cnt = 0;
//		while ( (r = dataStream.nextRecord() ) != null ) {
//			for( VariableMetadata var : variables ) {
////				cnt++;
////				
////				Integer specimenId = r.getValue("specimen_id", Integer.class);
////				Integer variableId = varMetadata.getVariableId();
////				Double value = r.getValue( varMetadata.getVariableName(), Double.class );
////				
////				//1. insert into tmp table
////				Query insert = createTmpValuesInsertQuery(create, transactionId, specimenId, variableId, value);
////				queries.add( insert );
////				
////				if( cnt % 2000 == 0 ) {
////					executeQueries(queries);
////				}
//				
//				cnt++;
//
//				String varName = var.getVariableName();
//				Integer varId = var.getVariableId();
//
//				Integer specimenId = r.getValue("specimen_id", Integer.class);
//
//				if ( var.isCategorical() ) {
//					String categoryCode = r.getValue(varName, String.class);
//					Integer categoryId = getCategoryId(varId, varName, categoryCode);
//
//					Query insert = createTmpCategoricalValueInsert(transactionId, specimenId, categoryId);
//					queries.add(insert);
//				} else if ( var.isNumeric() ) {
//					Double value = r.getValue(varName, Double.class);
//
//					Query insert = createTmpValuesInsertQuery(transactionId, specimenId, varId, value);
//					queries.add(insert);
//				}
//
//				if( cnt % 2000 == 0 ) {
//					executeQueries(queries);
//				}
//				
//			
//			}
//		}
//		
//		Query delete = createCurrentValuesDeleteQuery( transactionId );
//		queries.add(delete);
//
//		Query update = createCurrentValuesUpdateQuery( transactionId, false );
//		queries.add(update);
//
//		Query insert = createCurrentValuesInsertQuery( transactionId );
//		queries.add(insert);
//
//		Query deleteTmp = createTmpValuesDeleteQuery( transactionId );
//		queries.add(deleteTmp);
//
//		executeQueries(queries);
//		
//		long end = System.currentTimeMillis() - start;
//		getLog().debug("Updating specimen numerical values for variables "+variables.toString()+" executed in " + TimeUnit.MILLISECONDS.toSeconds(end)+" seconds");
//	}

//	@Transactional
//	private void executeQueries(ArrayList<Query> queries) {
//		Factory create = getJooqFactory();
//		
//		create.batch( queries ).execute();
//		queries.clear();
//	}
//
//	@Transactional
//	private Query createTmpValuesInsertQuery(int transactionId, Integer specimenId, Integer variableId, Double value) {
//		Factory create = getJooqFactory();
//		
//		return create
//			.insertInto(TNV, TNV.TRANSACTION_ID, TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE)
//			.values(transactionId, specimenId, variableId, value);
//	}
//	
//	@Transactional
//	private Query createTmpCategoricalValueInsert(int transactionId, Integer objectId, Integer categoryId) {
//		Factory create = getJooqFactory();
//		return create
//			.insertInto(TCV, TCV.TRANSACTION_ID, TCV.OBJECT_ID, TCV.CATEGORY_ID)
//			.values(transactionId, objectId, categoryId);
//	}
	
//	@Transactional
//	private Query createCurrentValuesDeleteQuery(int transactionId) {
//		Factory create = getJooqFactory();
//		
//		return create.delete( SNV )
//				.where( 
//						SNV.ORIGINAL.isFalse()
//						.and(
//							Factory.row(SNV.SPECIMEN_ID, SNV.VARIABLE_ID)
//									.in( 
//										create.select(TNV.OBJECT_ID,TNV.VARIABLE_ID)
//										.from(TNV)
//										.where( TNV.TRANSACTION_ID.eq( transactionId )) )
//						)
//				);
//	}
//	
//	@Transactional
//	private Query createCurrentValuesUpdateQuery(int transactionId, boolean currentValue) {
//		Factory create = getJooqFactory();
//		
//		return 
//				create
//				.update( SNV )
//				.set( SNV.CURRENT, currentValue )
//				.where(
//						Factory.row(SNV.SPECIMEN_ID, SNV.VARIABLE_ID)
//						.in(
//								create.select(TNV.OBJECT_ID, TNV.VARIABLE_ID)
//								.from(TNV)
//								.where( TNV.TRANSACTION_ID.eq( transactionId ) ) 
//							)
//					);
//	}
//
//	@Transactional
//	private Query createCurrentValuesInsertQuery(int transactionId) {
//		Factory create = getJooqFactory();
//		return create
//			.insertInto( SNV, SNV.SPECIMEN_ID, SNV.VARIABLE_ID, SNV.VALUE, SNV.ORIGINAL, SNV.CURRENT )
//			.select( 
//					create
//						.select( TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE, Factory.value(false, Boolean.class), Factory.value(true, Boolean.class) )
//						.from( TNV )
//						.where( TNV.TRANSACTION_ID.eq(transactionId) )
//					);
//	}

//	@Transactional
//	private Query createTmpValuesDeleteQuery(int transactionId) {
//		Factory create = getJooqFactory();
//		return create.delete( TNV ).where( TNV.TRANSACTION_ID.eq(transactionId) );
//	}
	
	
//	@Transactional
//	private Integer getCategoryId(Integer varId, String varName, String categoryCode) {
//		List<org.openforis.calc.persistence.jooq.tables.pojos.CategoryView> values = categories.get(varName);
//		if( values == null ) {
//			Factory create = getJooqFactory();
//			
//			values = create
//					.select( CAT.getFields() )
//					.from( CAT )
//					.where( CAT.VARIABLE_ID.eq(varId) )
//					.fetch()
//					.into(org.openforis.calc.persistence.jooq.tables.pojos.CategoryView.class);
//
//			categories.put(varName, values);
//		} 
//		
//		for ( org.openforis.calc.persistence.jooq.tables.pojos.CategoryView c : values ) {
//			if( c.getCategoryCode().equals(categoryCode) ){
//				return c.getCategoryId();
//			}
//		}
//		
//		return null;
//	}
}
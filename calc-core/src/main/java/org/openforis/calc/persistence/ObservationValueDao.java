/**
 * 
 */
package org.openforis.calc.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.CategoryView;
import org.openforis.commons.io.flat.FlatDataStream;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
@Component
@SuppressWarnings("rawtypes")
public abstract class ObservationValueDao extends JooqDaoSupport {
	
	private static final CategoryView CAT = CategoryView.CATEGORY_VIEW;
	
	protected org.openforis.calc.persistence.jooq.tables.TmpNumericValue TNV = org.openforis.calc.persistence.jooq.tables.TmpNumericValue.TMP_NUMERIC_VALUE; 
	protected org.openforis.calc.persistence.jooq.tables.TmpCategoricalValue TCV = org.openforis.calc.persistence.jooq.tables.TmpCategoricalValue.TMP_CATEGORICAL_VALUE; 
	
	private Map<Integer, List<org.openforis.calc.persistence.jooq.tables.pojos.CategoryView>> categories;

	@SuppressWarnings("unchecked")
	protected ObservationValueDao() {
		super(null, null);
		categories = new HashMap<Integer, List<org.openforis.calc.persistence.jooq.tables.pojos.CategoryView>>();
	}

	
	@Transactional
	public void updateCurrentValues(Integer obsUnitId, FlatDataStream dataStream, List<VariableMetadata> variables) throws IOException {
		long start = System.currentTimeMillis();
		ArrayList<Query> queries = new ArrayList<Query>();

		Factory create = getJooqFactory();

		int transactionId = create.nextval(Sequences.TRANSACTION_ID_SEQ).intValue();

		FlatRecord r = null;
		int cnt = 0;
		while ( (r = dataStream.nextRecord()) != null ) {
			for ( VariableMetadata var : variables ) {
				cnt++;

				String varName = var.getVariableName();
				Integer varId = var.getVariableId();

				Integer plotSectionId = r.getValue( getIdColumn(), Integer.class);

				if ( var.isCategorical() ) {
					String categoryCode = r.getValue(varName, String.class);
					Integer categoryId = getCategoryId(varId, categoryCode);

					Query insert = createTmpCategoricalValueInsert(transactionId, plotSectionId, categoryId);
					queries.add(insert);
				} else if ( var.isNumeric() ) {
					Double value = r.getValue(varName, Double.class);

					Query insert = createTmpNumericValueInsert(transactionId, plotSectionId, varId, value);
					queries.add(insert);
				}

				if( cnt % 2000 == 0 ) {
					executeQueries(queries);
				}
				
			}
		}
		
		Query deleteNV = createCurrentNumericValuesDelete(transactionId);
		queries.add(deleteNV);
		Query deleteCV = createCurrentCategoricalValuesDelete(transactionId);
		queries.add(deleteCV);
		
		Query updateNV = createCurrentNumericValuesUpdate(transactionId, false);
		queries.add(updateNV);
		Query updateCV = createCurrentCategoricalValuesUpdate(transactionId, false);
		queries.add(updateCV);

		Query insertNV = createCurrentNumericValuesInsert( transactionId );
		queries.add(insertNV);
		Query insertCV = createCurrentCategoricalValuesInsert( transactionId );
		queries.add(insertCV);
		
		Query deleteTmp = createTmpValuesDelete( transactionId );
		queries.add(deleteTmp);

		executeQueries(queries);
		
		long end = System.currentTimeMillis() - start;
		getLog().debug("Updating values for variables "+variables.toString()+" executed in " + TimeUnit.MILLISECONDS.toSeconds(end)+" seconds");
		
	}
	
	protected abstract Query createCurrentCategoricalValuesInsert(int transactionId);


	protected abstract Query createCurrentNumericValuesInsert(int transactionId) ;


	protected abstract Query createCurrentCategoricalValuesUpdate(int transactionId, boolean b) ;


	protected abstract Query createCurrentNumericValuesUpdate(int transactionId, boolean b) ;


	protected abstract Query createCurrentCategoricalValuesDelete(int transactionId) ;


	protected abstract Query createCurrentNumericValuesDelete(int transactionId) ;

	protected abstract String getIdColumn();

	@Transactional
	private void executeQueries(ArrayList<Query> queries) {
		Factory create = getJooqFactory();
		create.batch( queries ).execute();
		queries.clear();
	}
	
	@Transactional
	private Integer getCategoryId(Integer varId, String categoryCode) {
		List<org.openforis.calc.persistence.jooq.tables.pojos.CategoryView> values = categories.get(varId);
		if( values == null ) {
			Factory create = getJooqFactory();
			
			values = create
					.select( CAT.getFields() )
					.from( CAT )
					.where( CAT.VARIABLE_ID.eq(varId) )
					.fetch()
					.into(org.openforis.calc.persistence.jooq.tables.pojos.CategoryView.class);

			categories.put(varId, values);
		} 
		
		for ( org.openforis.calc.persistence.jooq.tables.pojos.CategoryView c : values ) {
			if( c.getCategoryCode().equals(categoryCode) ){
				return c.getCategoryId();
			}
		}
		
		return null;
	}

	@Transactional
	private Query createTmpNumericValueInsert(int transactionId, Integer objectId, Integer variableId, Double value) {
		Factory create = getJooqFactory();
		return create
			.insertInto(TNV, TNV.TRANSACTION_ID, TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE)
			.values(transactionId, objectId, variableId, value);
	}
	
	@Transactional
	private Query createTmpCategoricalValueInsert(int transactionId, Integer objectId, Integer categoryId) {
		Factory create = getJooqFactory();
		return create
			.insertInto(TCV, TCV.TRANSACTION_ID, TCV.OBJECT_ID, TCV.CATEGORY_ID)
			.values(transactionId, objectId, categoryId);
	}

	@Transactional
	private Query createTmpValuesDelete(int transactionId) {
		Factory create = getJooqFactory();
		return create.delete( TNV ).where( TNV.TRANSACTION_ID.eq(transactionId) );
	}
	
}

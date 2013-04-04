/**
 * 
 */
package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_CATEGORICAL_VALUE;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.persistence.jooq.tables.Specimen;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
@Component
public class SpecimenValueDao extends ObservationValueDao {
	
	private final static org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue SNV = SPECIMEN_NUMERIC_VALUE;
	private final static org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue SCV = SPECIMEN_CATEGORICAL_VALUE;
	
	@Transactional
	protected Query createCurrentNumericValuesDelete(int transactionId) {
		Factory create = getJooqFactory();
		return create.delete( SNV )
				.where( 
						SNV.ORIGINAL.isFalse()
						.and(
							Factory.row( SNV.SPECIMEN_ID, SNV.VARIABLE_ID )
									.in( 
										create.select( TNV.OBJECT_ID,TNV.VARIABLE_ID )
										.from( TNV )
										.where( TNV.TRANSACTION_ID.eq(transactionId) ) 
										)
						)
				);
	}
	
	@Transactional
	protected Query createCurrentCategoricalValuesDelete(int transactionId) {
		Factory create = getJooqFactory();
		return create.delete( SCV )
				.where( 
						SCV.ORIGINAL.isFalse()
						.and(
							Factory.row( SCV.SPECIMEN_ID, SCV.CATEGORY_ID )
									.in( 
										create.select( TCV.OBJECT_ID, TCV.CATEGORY_ID )
										.from( TCV )
										.where( TCV.TRANSACTION_ID.eq( transactionId )) )
						)
				);
	}
	
	@Transactional
	protected Query createCurrentNumericValuesUpdate(int transactionId, boolean currentValue) {
		Factory create = getJooqFactory();
		return 
				create
				.update( SNV )
				.set( SNV.CURRENT, currentValue )
				.where(
						Factory.row( SNV.SPECIMEN_ID, SNV.VARIABLE_ID)
						.in(
								create.select( TNV.OBJECT_ID, TNV.VARIABLE_ID )
								.from(TNV)
								.where( TNV.TRANSACTION_ID.eq( transactionId ) ) 
							)
					);
	}

	@Transactional
	protected Query createCurrentCategoricalValuesUpdate(int transactionId, boolean currentValue) {
		Factory create = getJooqFactory();
		return 
				create
				.update( SCV )
				.set( SCV.CURRENT, currentValue )
				.where(
						Factory.row( SCV.SPECIMEN_ID, SCV.CATEGORY_ID )
						.in(
								create.select( TCV.OBJECT_ID, TCV.CATEGORY_ID )
								.from(TCV)
								.where( TCV.TRANSACTION_ID.eq( transactionId ) ) 
							)
					);
	}
	
	protected Query createCurrentNumericValuesInsert(int transactionId) {
		Factory create = getJooqFactory();
		return create
			.insertInto( SNV, SNV.SPECIMEN_ID, SNV.VARIABLE_ID, SNV.VALUE, SNV.ORIGINAL, SNV.CURRENT )
			.select( 
					create
						.select( TNV.OBJECT_ID, TNV.VARIABLE_ID, TNV.VALUE, Factory.value(false, Boolean.class), Factory.value(true, Boolean.class) )
						.from( TNV )
						.where( TNV.TRANSACTION_ID.eq(transactionId) )
					);
	}
	
	protected Query createCurrentCategoricalValuesInsert(int transactionId) {
		Factory create = getJooqFactory();
		return create
			.insertInto( SCV, SCV.SPECIMEN_ID, SCV.CATEGORY_ID, SCV.ORIGINAL, SCV.CURRENT )
			.select( 
					create
						.select( TCV.OBJECT_ID, TCV.CATEGORY_ID, Factory.value(false, Boolean.class), Factory.value(true, Boolean.class) )
						.from( TCV )
						.where( TCV.TRANSACTION_ID.eq(transactionId) )
					);
	}

	@Override
	protected String getIdColumn() {
		return Specimen.SPECIMEN.SPECIMEN_ID.getName();
	}
}

/**
 * 
 */
package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_NUMERIC_VALUE;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.persistence.jooq.tables.PlotSection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
@Component
public class PlotSectionValueDao extends ObservationValueDao {
	
	private final static org.openforis.calc.persistence.jooq.tables.PlotNumericValue PNV = PLOT_NUMERIC_VALUE;
	private final static org.openforis.calc.persistence.jooq.tables.PlotCategoricalValue PCV = PLOT_CATEGORICAL_VALUE;
	
	@Transactional
	protected Query createCurrentNumericValuesDelete(int transactionId) {
		Factory create = getJooqFactory();
		return create.delete( PNV )
				.where( 
						PNV.ORIGINAL.isFalse()
						.and(
							Factory.row(PNV.PLOT_SECTION_ID, PNV.VARIABLE_ID)
									.in( 
										create.select(TNV.OBJECT_ID,TNV.VARIABLE_ID)
										.from(TNV)
										.where( TNV.TRANSACTION_ID.eq( transactionId )) )
						)
				);
	}
	
	@Transactional
	protected Query createCurrentCategoricalValuesDelete(int transactionId) {
		Factory create = getJooqFactory();
		return create.delete( PCV )
				.where( 
						PCV.ORIGINAL.isFalse()
						.and(
							Factory.row( PCV.PLOT_SECTION_ID, PCV.CATEGORY_ID )
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
				.update( PNV )
				.set( PNV.CURRENT, currentValue )
				.where(
						Factory.row(PNV.PLOT_SECTION_ID, PNV.VARIABLE_ID)
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
				.update( PCV )
				.set( PCV.CURRENT, currentValue )
				.where(
						Factory.row( PCV.PLOT_SECTION_ID, PCV.CATEGORY_ID )
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
			.insertInto( PNV, PNV.PLOT_SECTION_ID, PNV.VARIABLE_ID, PNV.VALUE, PNV.ORIGINAL, PNV.CURRENT )
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
			.insertInto( PCV, PCV.PLOT_SECTION_ID, PCV.CATEGORY_ID, PCV.ORIGINAL, PCV.CURRENT )
			.select( 
					create
						.select( TCV.OBJECT_ID, TCV.CATEGORY_ID, Factory.value(false, Boolean.class), Factory.value(true, Boolean.class) )
						.from( TCV )
						.where( TCV.TRANSACTION_ID.eq(transactionId) )
					);
	}
	
	@Override
	protected String getIdColumn() {
		return PlotSection.PLOT_SECTION.PLOT_SECTION_ID.getName();
	}
}

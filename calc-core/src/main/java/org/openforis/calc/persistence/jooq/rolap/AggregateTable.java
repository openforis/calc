package org.openforis.calc.persistence.jooq.rolap;

import java.math.BigDecimal;

import org.jooq.Field;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public abstract class AggregateTable<T extends FactTable> extends FactTable {

	private static final long serialVersionUID = 1L;
	private T factTable;

	public final Field<BigDecimal> AGG_COUNT = createFixedMeasureField("agg_cnt");

	AggregateTable(T factTable, String infix) {
		super(
				factTable.getSchema().getName(), 
				factTable.getObservationUnitMetadata().getAggregateTableName(infix), 
				factTable.getObservationUnitMetadata());
		this.factTable = factTable;
	}
	
	public T getFactTable() {
		return factTable;
	}
}

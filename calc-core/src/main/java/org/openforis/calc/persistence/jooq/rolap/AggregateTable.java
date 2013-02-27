package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

/**
 * 
 * @author G. Miceli
 *
 */
public class AggregateTable extends FactTable {

	private static final long serialVersionUID = 1L;
	private FactTable factTable;

	AggregateTable(FactTable factTable, 
			String infix, List<String> measures, List<String> dimensions) {
		super(
				factTable.getSchema().getName(), 
				factTable.getObservationUnitMetadata().getAggregateTableName(infix), 
				factTable.getObservationUnitMetadata(), 
				measures, dimensions, null);
		this.factTable = factTable;
	}
	
	public FactTable getFactTable() {
		return factTable;
	}
}

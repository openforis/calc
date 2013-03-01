package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Record;
import org.jooq.TableField;

import static org.jooq.impl.SQLDataType.*;
/**
 * 
 * @author G. Miceli
 *
 */
public class AggregateTable extends FactTable {

	private static final long serialVersionUID = 1L;
	private FactTable factTable;

	public final TableField<Record, Integer> AGG_COUNT = createField("agg_cnt", INTEGER);

	AggregateTable(FactTable factTable, 
			String infix, List<String> measures, List<String> dimensions) {
		super(
				factTable.getSchema().getName(), 
				factTable.getObservationUnitMetadata().getAggregateTableName(infix), 
				factTable.getObservationUnitMetadata(), 
				measures, dimensions);
		this.factTable = factTable;
	}
	
	public FactTable getFactTable() {
		return factTable;
	}
}

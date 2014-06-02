package org.openforis.calc.schema;

/**
 * 
 * @author Mino Togna
 * 
 */
public class SamplingUnitAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;

	SamplingUnitAggregateTable(DataTable factTable) {
		super(factTable, getName(factTable));
	}

	private static String getName(DataTable factTable) {
		String entityName = factTable.getEntity().getName();
		return String.format("_%s_plot_agg", entityName);
	}

}

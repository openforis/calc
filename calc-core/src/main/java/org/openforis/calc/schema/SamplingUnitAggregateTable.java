package org.openforis.calc.schema;

/**
 * 
 * @author Mino Togna
 * 
 */
public class SamplingUnitAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;

	SamplingUnitAggregateTable( DataTable table ){
		super(table, getName(table));
	}

	private static String getName( DataTable table ){
		String entityName = table.getEntity().getName();
		return String.format("_%s_plot_agg", entityName);
	}

}

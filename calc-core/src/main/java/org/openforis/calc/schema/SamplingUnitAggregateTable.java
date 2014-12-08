package org.openforis.calc.schema;

import org.jooq.Field;
import org.jooq.impl.SQLDataType;

/**
 * 
 * @author Mino Togna
 * 
 */
public class SamplingUnitAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;
	
	private Field<String> clusterField;
	
	SamplingUnitAggregateTable( DataTable table ){
		super(table, getName(table));
		
		createClusterField();
		createSamplingUnitIdField();
	}

	private static String getName( DataTable table ){
		String entityName = table.getEntity().getName();
		return String.format("_%s_plot_agg", entityName);
	}

	private void createClusterField() {
		this.clusterField = createField( "_cluster", SQLDataType.VARCHAR, this );
	}

	public Field<String> getClusterField() {
		return clusterField;
	}
	
}

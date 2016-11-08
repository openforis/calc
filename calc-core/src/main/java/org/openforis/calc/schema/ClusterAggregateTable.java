/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.psql.Psql;

/**
 * @author M. Togna
 *
 */
public class ClusterAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;
	private TableField<Record, BigDecimal> weightField;

	/**
	 * @param sourceTable
	 * @param name
	 * @param aoiLevel
	 */
	public ClusterAggregateTable(DataTable sourceTable) {
		super(sourceTable, getName(sourceTable));
		
		createClusterField();
		
		if( sourceTable.getEntity().isSamplingUnit() ){
			this.weightField = createField( WEIGHT_COLUMN, Psql.DOUBLE_PRECISION, this );
		}
	}

	private static String getName(DataTable table) {
		String entityName = table.getEntity().getName();
		return String.format("_%s_cluster_agg", entityName);
	}
	
	@Override
	public Field<BigDecimal> getWeightField() {
		return this.weightField;
	}

	

}

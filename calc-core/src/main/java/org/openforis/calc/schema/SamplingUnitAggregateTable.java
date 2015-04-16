package org.openforis.calc.schema;

import java.util.List;

import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;

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
		createPsuFields();
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
	
	@Override
	protected void createPsuFields() {
		if( getWorkspace().has2StagesSamplingDesign() ){

			SamplingDesign samplingDesign = getWorkspace().getSamplingDesign();
			List<ColumnJoin> columns = samplingDesign.getTwoStagesSettingsObject().getSamplingUnitPsuJoinColumns();
			for (ColumnJoin columnJoin : columns) {
				Field<?> sourceField = this.getSourceTable().field( columnJoin.getColumn() );
				Field<?> field = super.copyField( sourceField );
				addPsuField( field );
			}
		}
	
	}
	
}

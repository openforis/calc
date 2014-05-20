package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collection;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author M. Togna
 * 
 */
public class ResultTable extends DataTable {

	private static final long serialVersionUID = 1L;

	public static final String PLOT_AREA_COLUMN_NAME = "plot_area";

	private TableField<Record, BigDecimal> plotArea;
	
	public ResultTable( Entity entity, DataSchema schema, boolean temporary ) {
		super(entity, (temporary?entity.getTemporaryResultsTable():entity.getResultsTable()), schema);
		createPrimaryKeyField();
		createQuantityFields();
		
		// for now it always creates the plot area column
//		if( entity.getPlotAreaRScript() != null ){
			plotArea = super.createField( PLOT_AREA_COLUMN_NAME, Psql.DOUBLE_PRECISION , this );
//		}
	}
	
	private void createQuantityFields() {
		Collection<QuantitativeVariable> quantitativeVariables = getEntity().getDefaultProcessingChainOutputVariables();
		for (QuantitativeVariable var : quantitativeVariables) {
			createQuantityField( var, var.getOutputValueColumn() );
		}
	}
	
	public ResultTable(Entity entity, DataSchema schema) {
		this(entity, schema, false);
	}
	
	public TableField<Record, BigDecimal> getPlotArea() {
		return plotArea;
	}
}

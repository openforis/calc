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
	
	public ResultTable(Entity entity, DataSchema schema, boolean temporary) {
		super(entity, (temporary?entity.getTemporaryResultsTable():entity.getResultsTable()), schema);
		createPrimaryKeyField();
//		createParentIdField();
//		createCategoryValueFields(entity, true);
		createQuantityFields();
		
		if( entity.getPlotAreaRScript() != null ){
//			Field<BigDecimal> field = createValueField(var, Psql.DOUBLE_PRECISION, valueColumn);
			plotArea = super.createField( PLOT_AREA_COLUMN_NAME, Psql.DOUBLE_PRECISION , this );
		}
		
//		createCoordinateFields();
//		createTextFields();
	}
	
	private void createQuantityFields() {
		Collection<QuantitativeVariable> quantitativeVariables = getEntity().getOutputVariables();
		for (QuantitativeVariable var : quantitativeVariables) {
			createQuantityField(var, var.getOutputValueColumn());
		}
	}

	public ResultTable(Entity entity, DataSchema schema) {
		this(entity, schema, false);
	}
	
	public TableField<Record, BigDecimal> getPlotArea() {
		return plotArea;
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	protected <T> Field<T> createValueField(Variable<?> var, DataType<T> valueType, String valueColumn) {
//		DataType<?> sqlType;
//		if ( var.getOriginalId() == null ) {
//			//user defined variable
//			sqlType = valueType;;
//		} else {
//			// we don't know the datatype of columns in the input schema...
//			sqlType = SQLDataType.OTHER;
//		}
//		return (Field<T>) super.createValueField(var, sqlType, valueColumn);
//	}

}

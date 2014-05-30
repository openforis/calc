package org.openforis.calc.schema;

import java.util.List;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.Variable;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class InputTable extends DataTable {

	private static final long serialVersionUID = 1L;

	public InputTable(Entity entity, DataSchema schema) {
		super(entity, entity.getDataTable(), schema);
		createPrimaryKeyField();
		createParentIdField();
		createCategoryValueFields(entity, true);
		createCategoryIdFields(entity, true);
		createQuantityFields(true);
		createCoordinateFields();
		createTextFields();
		
		createWeightField();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> Field<T> createValueField(Variable<?> var, DataType<T> valueType, String valueColumn) {
		DataType<?> sqlType;
		if ( var.getOriginalId() == null ) {
			//user defined variable
			sqlType = valueType;;
		} else {
			// we don't know the datatype of columns in the input schema...
			sqlType = SQLDataType.OTHER;
		}
		return (Field<T>) super.createValueField(var, sqlType, valueColumn);
	}
	
	protected void createCategoryIdFields(Entity entity, boolean input) {
		List<CategoricalVariable<?>> variables = entity.getCategoricalVariables();
		for ( CategoricalVariable<?> var : variables ) {
			if( !var.isUserDefined() ){
				String valueColumn = input ? var.getInputValueColumn() : var.getOutputValueColumn();
				if ( valueColumn != null ) {
					if ( var instanceof BinaryVariable ) {
//					createBinaryCategoryValueField((BinaryVariable) var, valueColumn);
					} else if ( var instanceof MultiwayVariable ) {
						createCategoryIdField((MultiwayVariable) var, ((MultiwayVariable) var).getInputCategoryIdColumn());
					}
				}
			}
		}
	}
	
	protected void createCategoryValueFields(Entity entity, boolean input) {
		List<CategoricalVariable<?>> variables = entity.getCategoricalVariables();
		for ( CategoricalVariable<?> var : variables ) {
			if( !var.isUserDefined() ){
				String valueColumn = input ? var.getInputValueColumn() : var.getOutputValueColumn();
				if ( valueColumn != null ) {
					if ( var instanceof BinaryVariable ) {
						createBinaryCategoryValueField((BinaryVariable) var, valueColumn);
					} else if ( var instanceof MultiwayVariable ) {
						createCategoryValueField((MultiwayVariable) var, valueColumn);
					}
				}
			}
		}
	}

}

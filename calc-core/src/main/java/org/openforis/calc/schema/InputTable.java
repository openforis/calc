package org.openforis.calc.schema;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class InputTable extends DataTable {

	private static final long serialVersionUID = 1L;

	public InputTable(Entity entity, InputSchema schema) {
		super(entity, entity.getDataTable(), schema);
		createPrimaryKeyField();
		createParentIdField();
		createCategoryValueFields(entity, true);
		createCategoryIdFields(entity, true);
		createQuantityFields(true);
		createCoordinateFields();
		createTextFields();
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

}

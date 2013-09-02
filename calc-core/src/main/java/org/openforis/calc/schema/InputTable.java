package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.VARCHAR;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
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

	public InputTable(Entity entity, InputSchema schema) {
		super(entity, entity.getDataTable(), schema);
		createPrimaryKeyField();
		createParentIdField();		
		createCategoryValueFields(entity, true);
		createQuantityFields(true);
		createCoordinateFields();
	}
	

	@Override
	protected <T> Field<T> createValueField(Variable<?> var, DataType<T> valueType, String valueColumn) {
		// we don't know the datatype of columns in the input schema...
		return (Field) super.createValueField(var, SQLDataType.OTHER, valueColumn);
	}
}

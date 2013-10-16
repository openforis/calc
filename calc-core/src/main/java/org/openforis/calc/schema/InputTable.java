package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.VARCHAR;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.TextVariable;
import org.openforis.calc.metadata.Variable;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class InputTable extends DataTable {

	private static final long serialVersionUID = 1L;

	private Map<TextVariable, Field<String>> textFields;

	public InputTable(Entity entity, InputSchema schema) {
		super(entity, entity.getDataTable(), schema);
		createPrimaryKeyField();
		createParentIdField();
		createCategoryValueFields(entity, true);
		createQuantityFields(true);
		createCoordinateFields();
		createTextFields();
		createUserDefinedVariableFields();
	}

	private void createTextFields() {
		textFields = new HashMap<TextVariable, Field<String>>();

		List<TextVariable> vars = getEntity().getTextVariables();
		for ( TextVariable var : vars ) {
			String name = var.getName();
			Field<String> fld = createField(name, VARCHAR.length(255), this);
			textFields.put(var, fld);
		}
	}

	private void createUserDefinedVariableFields() {
		Collection<Variable<?>> userDefinedVariables = getEntity().getUserDefinedVariables();
		for (Variable<?> variable : userDefinedVariables) {
			String name = variable.getInputValueColumn();
			if ( variable instanceof QuantitativeVariable ) {
				createQuantityField((QuantitativeVariable) variable, name);
			} else {
				//TODO
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> Field<T> createValueField(Variable<?> var, DataType<T> valueType, String valueColumn) {
		// we don't know the datatype of columns in the input schema...
		return (Field<T>) super.createValueField(var, SQLDataType.OTHER, valueColumn);
	}

	public Collection<Field<String>> getTextFields() {
		return Collections.unmodifiableCollection(textFields.values());
	}
}

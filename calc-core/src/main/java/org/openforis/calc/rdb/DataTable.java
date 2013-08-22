/**
 * 
 */
package org.openforis.calc.rdb;

import static org.jooq.impl.SQLDataType.BOOLEAN;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.util.List;

import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;


/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class DataTable extends AbstractTable {

	private static final long serialVersionUID = 1L;

	private final TableField<Record, Integer> idField;
	
	private Entity entity;

	private UniqueKey<Record> primaryKey;	
	
	@SuppressWarnings("unchecked")
	public DataTable(Entity entity, RelationalSchema schema) {
		super(entity.getDataTable(), schema);
		this.entity = entity;
		this.idField = createField(entity.getIdColumn(), INTEGER, this);
		this.primaryKey = KeyFactory.newUniqueKey(this, idField);
	}
	
	protected void createVariableFields(boolean inputFieldsOnly) {
		List<Variable> variables = entity.getVariables();
		for (Variable var : variables) {
			if ( !inputFieldsOnly || var.isInput() ) {
				if ( var instanceof QuantitativeVariable ) {
					createValueField(var, DOUBLE_PRECISION);
				} else if ( var instanceof BinaryVariable ) {
					createValueField(var, BOOLEAN);
					createIdField((BinaryVariable) var);
				} else if ( var instanceof CategoricalVariable ) {
					createValueField(var, VARCHAR.length(255));
					createIdField((CategoricalVariable) var);
				}
			}
		}
	}

	private void createIdField(CategoricalVariable var) {
		String categoryIdColumn = var.getCategoryIdColumn();
		if ( categoryIdColumn != null ) {
			createField(categoryIdColumn, INTEGER, this);
		}
	}

	private void createValueField(Variable var, DataType<?> valueType) {
		String valueColumn = var.getValueColumn();
		if ( valueColumn != null ) {
			createField(valueColumn, valueType, this);
		}
	}

	public TableField<Record, Integer> getIdField() {
		return idField;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}
}
 
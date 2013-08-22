/**
 * 
 */
package org.openforis.calc.rdb;

import static org.jooq.impl.SQLDataType.BOOLEAN;
import static org.jooq.impl.SQLDataType.DOUBLE;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.util.List;

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
					createQuantitativeVariableField((QuantitativeVariable) var);
				} else if ( var instanceof BinaryVariable ) {
					createBinaryVariableFields((BinaryVariable) var);
				} else if ( var instanceof CategoricalVariable ) {
					createCategoricalVariableFields((CategoricalVariable) var);
				}
			}
		}
	}

	private void createQuantitativeVariableField(QuantitativeVariable var) {
		createField(var.getValueColumn(), DOUBLE, this);
	}

	private void createBinaryVariableFields(BinaryVariable var) {
		createField(var.getValueColumn(), BOOLEAN, this);
		createField(var.getCategoryIdColumn(), INTEGER, this);
	}

	private void createCategoricalVariableFields(CategoricalVariable var) {
		createField(var.getValueColumn(), VARCHAR.length(255), this);
		createField(var.getCategoryIdColumn(), INTEGER, this);
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
 
/**
 * 
 */
package org.openforis.calc.collect;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.SchemaImpl;
import org.openforis.calc.schema.AbstractTable;

/**
 * @author S. Ricci
 *
 */
@Deprecated
public class CollectCodeListTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private TableField<Record, Integer> idField;
	private TableField<Record, String> codeField;
	private TableField<Record, String> labelField;
	private TableField<Record, String> descriptionField;
	
	CollectCodeListTable(String name, String schema, String codeColumnName, 
			String labelColumnName, String descriptionColumnName) {
		super(name, new SchemaImpl(schema));
		
		String idColumnName = name + "_id";
		idField = createField(idColumnName, SQLDataType.INTEGER, this);
		codeField = createField(codeColumnName, SQLDataType.VARCHAR, this);
		labelField = createField(labelColumnName, SQLDataType.VARCHAR, this);
		descriptionField = createField(descriptionColumnName, SQLDataType.VARCHAR, this);
	}

	public TableField<Record, Integer> getIdField() {
		return idField;
	}

	public TableField<Record, String> getCodeField() {
		return codeField;
	}

	public TableField<Record, String> getLabelField() {
		return labelField;
	}

	public TableField<Record, String> getDescriptionField() {
		return descriptionField;
	}
	

}

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
 * @author M. Togna
 */
// @Deprecated
public class BooleanCodeListTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private TableField<Record, String> idField;
	private TableField<Record, String> codeField;
	private TableField<Record, String> labelField;
	// private TableField<Record, String> descriptionField;

	BooleanCodeListTable(String schema) {
		super("boolean_code", new SchemaImpl(schema));

		idField = createField("_id", SQLDataType.VARCHAR, this);
		codeField = createField("code", SQLDataType.VARCHAR, this);
		labelField = createField("label", SQLDataType.VARCHAR, this);
		// descriptionField = createField(", SQLDataType.VARCHAR, this);
	}

	public TableField<Record, String> getIdField() {
		return idField;
	}

	public TableField<Record, String> getCodeField() {
		return codeField;
	}

	public TableField<Record, String> getLabelField() {
		return labelField;
	}

	// public TableField<Record, String> getDescriptionField() {
	// return descriptionField;
	// }

}

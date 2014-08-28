/**
 * 
 */
package org.openforis.calc.schema;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class AbstractTable extends TableImpl<Record> {

	private static final long serialVersionUID = 1L;

	protected AbstractTable(String name, Schema schema) {
		super(name, schema);
	}

	public boolean hasField(String name) {
		return field(name) != null;
	}
	
	protected <T> TableField<Record, T> copyField(Field<T> field) {
		return createField(field.getName(), field.getDataType(), this);
	}
	
}

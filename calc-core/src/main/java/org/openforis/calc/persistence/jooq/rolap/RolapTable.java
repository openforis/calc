/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import static org.jooq.impl.SQLDataType.*;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UpdatableTableImpl;

/**
 * @author M. Togna
 * @author G. Miceli 
 *
 */
public abstract class RolapTable extends UpdatableTableImpl<Record> {
	
	private static final long serialVersionUID = 1L;

	public final Field<Integer> ID = createField("id", INTEGER);
	
	/**
	 * @param name
	 * @param schema
	 */
	RolapTable(String schema, String name) {
		super(name, new SchemaImpl(schema));
	}
	
//	private <T> Field<T> createField(String string, DataType<T> dataType) {
//		return createField(string.getName(), dataType);
//	}

	protected <T> Field<T> createField(Field<T> field) {
		return createField(field.getName(), field.getDataType());
	}

	protected <T> Field<T> createField(String name, DataType<T> type) {
		return createField(name, type, this);
	}
}

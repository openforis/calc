/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class AbstractTable extends TableImpl<Record> {

	private static final long serialVersionUID = 1L;

	protected static final DataType<BigDecimal> DOUBLE_PRECISION = SQLDataType.NUMERIC.precision(15, 5);

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

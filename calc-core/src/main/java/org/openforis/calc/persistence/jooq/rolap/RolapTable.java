/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jooq.Record;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UpdatableTableImpl;

/**
 * @author M. Togna
 * @author G. Miceli 
 *
 */
public abstract class RolapTable<R extends Record> extends UpdatableTableImpl<R> {
	
	private static final long serialVersionUID = 1L;

	public final org.jooq.TableField<R, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);
	
	private Class<R> recordType;
	/**
	 * @param name
	 * @param schema
	 */
	RolapTable(String schema, String name, Class<R> recordType) {
		super(name, new SchemaImpl(schema));
		this.recordType = recordType;
	}
	
	@Override
	public Class<R> getRecordType() {
		return recordType;
	}
	
	@SuppressWarnings("unchecked")
	public R newRecord() {
		try {
			Constructor<?> cons = recordType.getConstructor(getClass());
			return (R) cons.newInstance(this);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}

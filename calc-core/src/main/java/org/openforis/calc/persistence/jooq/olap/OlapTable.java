/**
 * 
 */
package org.openforis.calc.persistence.jooq.olap;

import org.jooq.Record;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UpdatableTableImpl;

/**
 * @author M. Togna
 *
 */
public abstract class OlapTable<R extends Record> extends UpdatableTableImpl<R> {
	
	private static final long serialVersionUID = 1L;

	public final org.jooq.TableField<R, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);
	
	/**
	 * @param name
	 * @param schema
	 */
	public OlapTable(String name, String schema) {
		super(name, new SchemaImpl(schema) );
	}

	
}

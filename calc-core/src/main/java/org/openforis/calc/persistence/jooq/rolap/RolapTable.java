/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UpdatableTableImpl;
import static org.jooq.impl.SQLDataType.*;

/**
 * @author M. Togna
 * @author G. Miceli 
 *
 */
public abstract class RolapTable extends UpdatableTableImpl<Record> {
	
	private static final long serialVersionUID = 1L;

	public final TableField<Record, Integer> ID = createField("id", INTEGER, this);
	
	/**
	 * @param name
	 * @param schema
	 */
	RolapTable(String schema, String name) {
		super(name, new SchemaImpl(schema));
	}
}

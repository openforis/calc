/**
 * 
 */
package org.openforis.calc.rdb;

import org.jooq.Record;
import org.jooq.impl.TableImpl;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class AbstractTable extends TableImpl<Record> {

	private static final long serialVersionUID = 1L;

	protected AbstractTable(String name, RelationalSchema schema) {
		super(name, schema);
	}

}

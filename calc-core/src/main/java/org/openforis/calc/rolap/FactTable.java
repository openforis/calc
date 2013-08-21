/**
 * 
 */
package org.openforis.calc.rolap;

import org.openforis.calc.rdb.AbstractTable;
import org.openforis.calc.rdb.RelationalSchema;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class FactTable extends AbstractTable {

	private static final long serialVersionUID = 1L;

	public FactTable(String name, RelationalSchema schema) {
		super(name, schema);
	}

}

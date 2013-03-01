/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import static org.jooq.impl.SQLDataType.*;

import org.jooq.Record;
import org.jooq.TableField;

/**
 * @author M. Togna
 * @author G. Miceli
 *
 */
public abstract class DimensionTable extends RolapTable {

	private static final long serialVersionUID = 1L;
	
	public final TableField<Record, String> CODE = createField("code", VARCHAR, this);
	public final TableField<Record, String> LABEL = createField("label", VARCHAR, this);

	/**
	 * @param name
	 * @param schema
	 * @param recordType 
	 */
	DimensionTable(String schema, String name) {
		super( schema, name );
	}
}

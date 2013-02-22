/**
 * 
 */
package org.openforis.calc.persistence.jooq.tables;

import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UpdatableTableImpl;
import org.openforis.calc.persistence.jooq.tables.records.DimensionRecord;

/**
 * @author Mino Togna
 *
 */
public class DimensionTable extends UpdatableTableImpl<DimensionRecord> {

	private static final long serialVersionUID = 1L;
	
	public final org.jooq.TableField<DimensionRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);
	
	public final org.jooq.TableField<DimensionRecord, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR, this);
	
	public final org.jooq.TableField<DimensionRecord, String> LABEL = createField("label", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * @param name
	 * @param schema
	 */
	public DimensionTable(String name, String schema) {
		super( name, new SchemaImpl(schema) );
	}

}

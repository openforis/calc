/**
 * 
 */
package org.openforis.calc.psql;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.TableImpl;

/**
 * @author Mino Togna
 * 
 */
public class InformationSchemaColumnsTable extends TableImpl<Record> {

	private static final long serialVersionUID = 1L;

	public final TableField<Record, String> TABLE_CATALOG = createField("table_catalog", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);
	public final TableField<Record, String> TABLE_SCHEMA = createField("table_schema", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);
	public final TableField<Record, String> TABLE_NAME = createField("table_name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);
	public final TableField<Record, String> COLUMN_NAME = createField("column_name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);
	public final TableField<Record, Integer> ORDINAL_POSITION = createField("ordinal_position", org.jooq.impl.SQLDataType.INTEGER, this);
	public final TableField<Record, String> IS_NULLABLE = createField("is_nullable", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);
	public final TableField<Record, String> DATA_TYPE = createField("data_type", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);

	/**
	 * @param name
	 * @param schema
	 */
	public InformationSchemaColumnsTable() {
		super("columns", new SchemaImpl("information_schema"));

	}

}

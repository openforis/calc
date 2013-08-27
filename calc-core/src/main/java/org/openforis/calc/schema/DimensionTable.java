package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;
import static org.openforis.calc.psql.Psql.DOUBLE_PRECISION;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DimensionTable extends AbstractTable {

	private static final long serialVersionUID = 1L;

	public final TableField<Record, Integer> ID = createField("id", INTEGER, this);
	public final TableField<Record, String> CODE = createField("code", VARCHAR.length(25), this);
	public final TableField<Record, String> CAPTION = createField("caption", VARCHAR.length(255), this);
	public final TableField<Record, String> DESCRIPTION = createField("description", VARCHAR.length(1024), this);
	public final TableField<Record, Integer> SORT_ORDER = createField("sort_order", INTEGER, this);
	public final TableField<Record, BigDecimal> VALUE = createField("value", DOUBLE_PRECISION, this);
	
	private UniqueKey<Record> primaryKey;
	
	@SuppressWarnings("unchecked")
	DimensionTable(String name, RelationalSchema schema) {
		super(name, schema);
		this.primaryKey = KeyFactory.newUniqueKey(this, ID);
	}

	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}
}

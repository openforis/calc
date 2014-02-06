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

	public TableField<Record, Integer> ID ;
	public TableField<Record, String> CODE ;
	public TableField<Record, String> CAPTION ;
	public TableField<Record, String> DESCRIPTION ;
	public TableField<Record, Integer> SORT_ORDER ;
	public TableField<Record, BigDecimal> VALUE ;
	
	private UniqueKey<Record> primaryKey;
	
	@SuppressWarnings("unchecked")
	DimensionTable(String name, RelationalSchema schema) {
		super(name, schema);
		this.primaryKey = KeyFactory.newUniqueKey(this, ID);
		
		initFields();
	}

	protected void initFields() {
		// TOpublic final TableField<Record, Integer> ID = createField("id", INTEGER, this);
		CODE = createField("code", VARCHAR.length(25), this);
		CAPTION = createField("caption", VARCHAR.length(255), this);
		DESCRIPTION = createField("description", VARCHAR.length(1024), this);
		SORT_ORDER = createField("sort_order", INTEGER, this);
		VALUE = createField("value", DOUBLE_PRECISION, this);
		
	}

	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}
}

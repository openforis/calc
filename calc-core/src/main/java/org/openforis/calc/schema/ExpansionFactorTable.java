package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class ExpansionFactorTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME = "_expf";
	
	public final TableField<Record,Integer> STRATUM_ID = createField("stratum_id", INTEGER, this);
	public final TableField<Record,Integer> AOI_ID = createField("aoi_id", INTEGER, this);
	public final TableField<Record,Integer> ENTITY_ID = createField("entity_id", INTEGER, this);
	public final TableField<Record,BigDecimal> EXPF = createField("expf", Psql.DOUBLE_PRECISION, this);
	@SuppressWarnings("unchecked")
	private final UniqueKey<Record> primaryKey = KeyFactory.newUniqueKey(this, STRATUM_ID, AOI_ID, ENTITY_ID);

	protected ExpansionFactorTable(Schema schema) {
		super(TABLE_NAME, schema);
	}
	
	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}
}

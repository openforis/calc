package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class ExpansionFactorTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME = "_%s_expf";
	
	public final TableField<Record,Integer> STRATUM = createField("stratum", INTEGER, this);
	public final TableField<Record,Integer> AOI_ID = createField("aoi_id", INTEGER, this);
	public final TableField<Record,BigDecimal> EXPF = createField("expf", Psql.DOUBLE_PRECISION, this);
	
	private AoiLevel aoiLevel;
	
//	@SuppressWarnings("unchecked")
//	private final UniqueKey<Record> primaryKey = KeyFactory.newUniqueKey(this, STRATUM_ID, AOI_ID, ENTITY_ID);
	@Deprecated
	public final TableField<Record,Integer> ENTITY_ID = createField("entity_id", INTEGER, this);
	@Deprecated
	public final TableField<Record,Integer> STRATUM_ID = createField("stratum_ID", INTEGER, this);
	
	protected ExpansionFactorTable(AoiLevel level, Schema schema) {
		super( String.format(TABLE_NAME, level.getNormalizedName()), schema);
		this.aoiLevel = level;
	}
	
	@Deprecated
	protected ExpansionFactorTable(Schema schema) {
		super(TABLE_NAME, schema);
	}

	public AoiLevel getAoiLevel() {
		return aoiLevel;
	}
	
//	@Override
//	public UniqueKey<Record> getPrimaryKey() {
//		return primaryKey;
//	}
}

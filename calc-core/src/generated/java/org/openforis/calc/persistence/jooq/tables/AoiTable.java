/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import org.openforis.calc.persistence.jooq.CalcSchema;
import org.openforis.calc.persistence.jooq.Keys;
import org.openforis.calc.persistence.jooq.tables.records.AoiRecord;


/**
 * Each area of interest (AOI) may be divided into sub-parts such that the 
 * sub-parts add up to the area of the whole (i.e. a compositional containment 
 * hierarchy)
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AoiTable extends TableImpl<AoiRecord> {

	private static final long serialVersionUID = 202670416;

	/**
	 * The reference instance of <code>calc.aoi</code>
	 */
	public static final AoiTable AOI = new AoiTable();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<AoiRecord> getRecordType() {
		return AoiRecord.class;
	}

	/**
	 * The column <code>calc.aoi.id</code>.
	 */
	public final TableField<AoiRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>calc.aoi.aoi_level_id</code>.
	 */
	public final TableField<AoiRecord, Integer> AOI_LEVEL_ID = createField("aoi_level_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>calc.aoi.parent_aoi_id</code>.
	 */
	public final TableField<AoiRecord, Integer> PARENT_AOI_ID = createField("parent_aoi_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calc.aoi.code</code>.
	 */
	public final TableField<AoiRecord, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>calc.aoi.shape</code>.
	 */
	public final TableField<AoiRecord, String> SHAPE = createField("shape", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>calc.aoi.total_area</code>.
	 */
	public final TableField<AoiRecord, BigDecimal> TOTAL_AREA = createField("total_area", org.jooq.impl.SQLDataType.NUMERIC.precision(15, 5), this, "");

	/**
	 * The column <code>calc.aoi.land_area</code>.
	 */
	public final TableField<AoiRecord, BigDecimal> LAND_AREA = createField("land_area", org.jooq.impl.SQLDataType.NUMERIC.precision(15, 5), this, "");

	/**
	 * The column <code>calc.aoi.caption</code>.
	 */
	public final TableField<AoiRecord, String> CAPTION = createField("caption", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * Create a <code>calc.aoi</code> table reference
	 */
	public AoiTable() {
		this("aoi", null);
	}

	/**
	 * Create an aliased <code>calc.aoi</code> table reference
	 */
	public AoiTable(String alias) {
		this(alias, AOI);
	}

	private AoiTable(String alias, Table<AoiRecord> aliased) {
		this(alias, aliased, null);
	}

	private AoiTable(String alias, Table<AoiRecord> aliased, Field<?>[] parameters) {
		super(alias, CalcSchema.CALC, aliased, parameters, "Each area of interest (AOI) may be divided into sub-parts such that the sub-parts add up to the area of the whole (i.e. a compositional containment hierarchy)");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<AoiRecord, Integer> getIdentity() {
		return Keys.IDENTITY_AOI;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<AoiRecord> getPrimaryKey() {
		return Keys.AOI_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<AoiRecord>> getKeys() {
		return Arrays.<UniqueKey<AoiRecord>>asList(Keys.AOI_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<AoiRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<AoiRecord, ?>>asList(Keys.AOI__AOI_LEVEL_FKEY, Keys.AOI__AOI_PARENT_FKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AoiTable as(String alias) {
		return new AoiTable(alias, this);
	}

	/**
	 * Rename this table
	 */
	public AoiTable rename(String name) {
		return new AoiTable(name, null);
	}
}

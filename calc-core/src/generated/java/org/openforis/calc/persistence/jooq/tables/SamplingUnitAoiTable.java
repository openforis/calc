/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.1.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SamplingUnitAoiTable extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -710460629;

	/**
	 * The singleton instance of <code>calc.sampling_unit_aoi</code>
	 */
	public static final org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable SAMPLING_UNIT_AOI = new org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>calc.sampling_unit_aoi.workspace_id</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> WORKSPACE_ID = createField("workspace_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>calc.sampling_unit_aoi.sampling_unit_id</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> SAMPLING_UNIT_ID = createField("sampling_unit_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>calc.sampling_unit_aoi.aoi_id</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> AOI_ID = createField("aoi_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * Create a <code>calc.sampling_unit_aoi</code> table reference
	 */
	public SamplingUnitAoiTable() {
		super("sampling_unit_aoi", org.openforis.calc.persistence.jooq.CalcSchema.CALC);
	}

	/**
	 * Create an aliased <code>calc.sampling_unit_aoi</code> table reference
	 */
	public SamplingUnitAoiTable(java.lang.String alias) {
		super(alias, org.openforis.calc.persistence.jooq.CalcSchema.CALC, org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return org.openforis.calc.persistence.jooq.Keys.SAMPLE_PLOT_AOI_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(org.openforis.calc.persistence.jooq.Keys.SAMPLE_PLOT_AOI_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(org.openforis.calc.persistence.jooq.Keys.SAMPLING_UNIT_AOI__SAMPLING_UNIT_AOI_WORKSPACE_FKEY, org.openforis.calc.persistence.jooq.Keys.SAMPLING_UNIT_AOI__SAMPLING_UNIT_AOI_UNIT_FKEY, org.openforis.calc.persistence.jooq.Keys.SAMPLING_UNIT_AOI__SAMPLING_UNIT_AOI_FKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable as(java.lang.String alias) {
		return new org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable(alias);
	}
}

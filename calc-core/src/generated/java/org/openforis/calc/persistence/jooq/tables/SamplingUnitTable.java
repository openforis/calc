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
public class SamplingUnitTable extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = 1751481570;

	/**
	 * The singleton instance of <code>calc.sampling_unit</code>
	 */
	public static final org.openforis.calc.persistence.jooq.tables.SamplingUnitTable SAMPLING_UNIT = new org.openforis.calc.persistence.jooq.tables.SamplingUnitTable();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>calc.sampling_unit.id</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>calc.sampling_unit.entity_id</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> ENTITY_ID = createField("entity_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>calc.sampling_unit.panel</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> PANEL = createField("panel", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>calc.sampling_unit.cluster</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> CLUSTER = createField("cluster", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);

	/**
	 * The column <code>calc.sampling_unit.unit_no</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> UNIT_NO = createField("unit_no", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);

	/**
	 * The column <code>calc.sampling_unit.location</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> LOCATION = createField("location", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);

	/**
	 * The column <code>calc.sampling_unit.shape</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> SHAPE = createField("shape", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);

	/**
	 * The column <code>calc.sampling_unit.sampling_phase</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> SAMPLING_PHASE = createField("sampling_phase", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>calc.sampling_unit.permanent</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Boolean> PERMANENT = createField("permanent", org.jooq.impl.SQLDataType.BOOLEAN, this);

	/**
	 * The column <code>calc.sampling_unit.stratum_id</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> STRATUM_ID = createField("stratum_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * Create a <code>calc.sampling_unit</code> table reference
	 */
	public SamplingUnitTable() {
		super("sampling_unit", org.openforis.calc.persistence.jooq.CalcSchema.CALC);
	}

	/**
	 * Create an aliased <code>calc.sampling_unit</code> table reference
	 */
	public SamplingUnitTable(java.lang.String alias) {
		super(alias, org.openforis.calc.persistence.jooq.CalcSchema.CALC, org.openforis.calc.persistence.jooq.tables.SamplingUnitTable.SAMPLING_UNIT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return org.openforis.calc.persistence.jooq.Keys.SAMPLING_UNIT_PKEY1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(org.openforis.calc.persistence.jooq.Keys.SAMPLING_UNIT_PKEY1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(org.openforis.calc.persistence.jooq.Keys.SAMPLING_UNIT__SAMPLING_UNIT_ENTITY_FKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.openforis.calc.persistence.jooq.tables.SamplingUnitTable as(java.lang.String alias) {
		return new org.openforis.calc.persistence.jooq.tables.SamplingUnitTable(alias);
	}
}

/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.6.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings("all")
public class SamplingUnitAoiRecord extends org.jooq.impl.UpdatableRecordImpl<org.openforis.calc.persistence.jooq.tables.records.SamplingUnitAoiRecord> {

	private static final long serialVersionUID = 96056057;

	/**
	 * The table column <code>calc.sampling_unit_aoi.sampling_unit_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT sampling_unit_aoi__aoi_sampling_unit_fkey
	 * FOREIGN KEY (sampling_unit_id)
	 * REFERENCES calc.sampling_unit (id)
	 * </pre></code>
	 */
	public void setSamplingUnitId(java.lang.Integer value) {
		setValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID, value);
	}

	/**
	 * The table column <code>calc.sampling_unit_aoi.sampling_unit_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT sampling_unit_aoi__aoi_sampling_unit_fkey
	 * FOREIGN KEY (sampling_unit_id)
	 * REFERENCES calc.sampling_unit (id)
	 * </pre></code>
	 */
	public java.lang.Integer getSamplingUnitId() {
		return getValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID);
	}

	/**
	 * Link this record to a given {@link org.openforis.calc.persistence.jooq.tables.records.SamplingUnitRecord 
	 * SamplingUnitRecord}
	 */
	public void setSamplingUnitId(org.openforis.calc.persistence.jooq.tables.records.SamplingUnitRecord value) {
		if (value == null) {
			setValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID, null);
		}
		else {
			setValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID, value.getValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitTable.SAMPLING_UNIT.ID));
		}
	}

	/**
	 * The table column <code>calc.sampling_unit_aoi.sampling_unit_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT sampling_unit_aoi__aoi_sampling_unit_fkey
	 * FOREIGN KEY (sampling_unit_id)
	 * REFERENCES calc.sampling_unit (id)
	 * </pre></code>
	 */
	public org.openforis.calc.persistence.jooq.tables.records.SamplingUnitRecord fetchSamplingUnitTable() {
		return create()
			.selectFrom(org.openforis.calc.persistence.jooq.tables.SamplingUnitTable.SAMPLING_UNIT)
			.where(org.openforis.calc.persistence.jooq.tables.SamplingUnitTable.SAMPLING_UNIT.ID.equal(getValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID)))
			.fetchOne();
	}

	/**
	 * The table column <code>calc.sampling_unit_aoi.aoi_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT sampling_unit_aoi__sampling_unit_aoi_fkey
	 * FOREIGN KEY (aoi_id)
	 * REFERENCES calc.aoi (id)
	 * </pre></code>
	 */
	public void setAoiId(java.lang.Integer value) {
		setValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.AOI_ID, value);
	}

	/**
	 * The table column <code>calc.sampling_unit_aoi.aoi_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT sampling_unit_aoi__sampling_unit_aoi_fkey
	 * FOREIGN KEY (aoi_id)
	 * REFERENCES calc.aoi (id)
	 * </pre></code>
	 */
	public java.lang.Integer getAoiId() {
		return getValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.AOI_ID);
	}

	/**
	 * Link this record to a given {@link org.openforis.calc.persistence.jooq.tables.records.AoiRecord 
	 * AoiRecord}
	 */
	public void setAoiId(org.openforis.calc.persistence.jooq.tables.records.AoiRecord value) {
		if (value == null) {
			setValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.AOI_ID, null);
		}
		else {
			setValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.AOI_ID, value.getValue(org.openforis.calc.persistence.jooq.tables.AoiTable.AOI.ID));
		}
	}

	/**
	 * The table column <code>calc.sampling_unit_aoi.aoi_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT sampling_unit_aoi__sampling_unit_aoi_fkey
	 * FOREIGN KEY (aoi_id)
	 * REFERENCES calc.aoi (id)
	 * </pre></code>
	 */
	public org.openforis.calc.persistence.jooq.tables.records.AoiRecord fetchAoiTable() {
		return create()
			.selectFrom(org.openforis.calc.persistence.jooq.tables.AoiTable.AOI)
			.where(org.openforis.calc.persistence.jooq.tables.AoiTable.AOI.ID.equal(getValue(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI.AOI_ID)))
			.fetchOne();
	}

	/**
	 * Create a detached SamplingUnitAoiRecord
	 */
	public SamplingUnitAoiRecord() {
		super(org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI);
	}
}

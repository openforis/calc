/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.6.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings("all")
public class AoiHierarchyTable extends org.jooq.impl.UpdatableTableImpl<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord> {

	private static final long serialVersionUID = 1644671024;

	/**
	 * The singleton instance of calc.aoi_hierarchy
	 */
	public static final org.openforis.calc.persistence.jooq.tables.AoiHierarchyTable AOI_HIERARCHY = new org.openforis.calc.persistence.jooq.tables.AoiHierarchyTable();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord> getRecordType() {
		return org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord.class;
	}

	/**
	 * The table column <code>calc.aoi_hierarchy.id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord, java.lang.Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>calc.aoi_hierarchy.workspace_id</code>
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT aoi_hierarchy__aoi_hierarchy_workspace_fkey
	 * FOREIGN KEY (workspace_id)
	 * REFERENCES calc.workspace (id)
	 * </pre></code>
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord, java.lang.Integer> WORKSPACE_ID = createField("workspace_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>calc.aoi_hierarchy.caption</code>
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord, java.lang.String> CAPTION = createField("caption", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * The table column <code>calc.aoi_hierarchy.description</code>
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord, java.lang.String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR, this);

	public AoiHierarchyTable() {
		super("aoi_hierarchy", org.openforis.calc.persistence.jooq.CalcTable.CALC);
	}

	public AoiHierarchyTable(java.lang.String alias) {
		super(alias, org.openforis.calc.persistence.jooq.CalcTable.CALC, org.openforis.calc.persistence.jooq.tables.AoiHierarchyTable.AOI_HIERARCHY);
	}

	@Override
	public org.jooq.Identity<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord, java.lang.Integer> getIdentity() {
		return org.openforis.calc.persistence.jooq.Keys.IDENTITY_AOI_HIERARCHY;
	}

	@Override
	public org.jooq.UniqueKey<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord> getMainKey() {
		return org.openforis.calc.persistence.jooq.Keys.AOI_HIERARCHY_PKEY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.UniqueKey<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord>>asList(org.openforis.calc.persistence.jooq.Keys.AOI_HIERARCHY_PKEY);
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.ForeignKey<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord, ?>>asList(org.openforis.calc.persistence.jooq.Keys.AOI_HIERARCHY__AOI_HIERARCHY_WORKSPACE_FKEY);
	}

	@Override
	public org.openforis.calc.persistence.jooq.tables.AoiHierarchyTable as(java.lang.String alias) {
		return new org.openforis.calc.persistence.jooq.tables.AoiHierarchyTable(alias);
	}
}
/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CategoryHierarchyTable extends org.jooq.impl.TableImpl<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord> {

	private static final long serialVersionUID = 1592172398;

	/**
	 * The singleton instance of <code>calc.category_hierarchy</code>
	 */
	public static final org.openforis.calc.persistence.jooq.tables.CategoryHierarchyTable CATEGORY_HIERARCHY = new org.openforis.calc.persistence.jooq.tables.CategoryHierarchyTable();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord> getRecordType() {
		return org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord.class;
	}

	/**
	 * The column <code>calc.category_hierarchy.id</code>.
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, java.lang.Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>calc.category_hierarchy.name</code>.
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>calc.category_hierarchy.caption</code>.
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, java.lang.String> CAPTION = createField("caption", org.jooq.impl.SQLDataType.VARCHAR.length(255).defaulted(true), this, "");

	/**
	 * The column <code>calc.category_hierarchy.description</code>.
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, java.lang.String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR.length(1024).defaulted(true), this, "");

	/**
	 * The column <code>calc.category_hierarchy.category_id</code>.
	 */
	public final org.jooq.TableField<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, java.lang.Long> CATEGORY_ID = createField("category_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaulted(true), this, "");

	/**
	 * Create a <code>calc.category_hierarchy</code> table reference
	 */
	public CategoryHierarchyTable() {
		this("category_hierarchy", null);
	}

	/**
	 * Create an aliased <code>calc.category_hierarchy</code> table reference
	 */
	public CategoryHierarchyTable(java.lang.String alias) {
		this(alias, org.openforis.calc.persistence.jooq.tables.CategoryHierarchyTable.CATEGORY_HIERARCHY);
	}

	private CategoryHierarchyTable(java.lang.String alias, org.jooq.Table<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord> aliased) {
		this(alias, aliased, null);
	}

	private CategoryHierarchyTable(java.lang.String alias, org.jooq.Table<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.openforis.calc.persistence.jooq.CalcSchema.CALC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, java.lang.Integer> getIdentity() {
		return org.openforis.calc.persistence.jooq.Keys.IDENTITY_CATEGORY_HIERARCHY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord> getPrimaryKey() {
		return org.openforis.calc.persistence.jooq.Keys.HIERARCHY_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord>>asList(org.openforis.calc.persistence.jooq.Keys.HIERARCHY_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.openforis.calc.persistence.jooq.tables.records.CategoryHierarchyRecord, ?>>asList(org.openforis.calc.persistence.jooq.Keys.CATEGORY_HIERARCHY__CATEGORY_HIERARCHY_CATEGORY_FK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.openforis.calc.persistence.jooq.tables.CategoryHierarchyTable as(java.lang.String alias) {
		return new org.openforis.calc.persistence.jooq.tables.CategoryHierarchyTable(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.openforis.calc.persistence.jooq.tables.CategoryHierarchyTable rename(java.lang.String name) {
		return new org.openforis.calc.persistence.jooq.tables.CategoryHierarchyTable(name, null);
	}
}

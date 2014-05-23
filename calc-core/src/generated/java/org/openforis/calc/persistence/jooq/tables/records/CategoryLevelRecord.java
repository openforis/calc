/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CategoryLevelRecord extends org.jooq.impl.UpdatableRecordImpl<org.openforis.calc.persistence.jooq.tables.records.CategoryLevelRecord> implements org.jooq.Record9<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = 916009149;

	/**
	 * Setter for <code>calc.category_level.id</code>.
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>calc.category_level.id</code>.
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>calc.category_level.hierarchy_id</code>.
	 */
	public void setHierarchyId(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>calc.category_level.hierarchy_id</code>.
	 */
	public java.lang.Integer getHierarchyId() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>calc.category_level.name</code>.
	 */
	public void setName(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>calc.category_level.name</code>.
	 */
	public java.lang.String getName() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>calc.category_level.code_column</code>.
	 */
	public void setCodeColumn(java.lang.String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>calc.category_level.code_column</code>.
	 */
	public java.lang.String getCodeColumn() {
		return (java.lang.String) getValue(3);
	}

	/**
	 * Setter for <code>calc.category_level.rank</code>.
	 */
	public void setRank(java.lang.Integer value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>calc.category_level.rank</code>.
	 */
	public java.lang.Integer getRank() {
		return (java.lang.Integer) getValue(4);
	}

	/**
	 * Setter for <code>calc.category_level.table_name</code>.
	 */
	public void setTableName(java.lang.String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>calc.category_level.table_name</code>.
	 */
	public java.lang.String getTableName() {
		return (java.lang.String) getValue(5);
	}

	/**
	 * Setter for <code>calc.category_level.id_column</code>.
	 */
	public void setIdColumn(java.lang.String value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>calc.category_level.id_column</code>.
	 */
	public java.lang.String getIdColumn() {
		return (java.lang.String) getValue(6);
	}

	/**
	 * Setter for <code>calc.category_level.caption_column</code>.
	 */
	public void setCaptionColumn(java.lang.String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>calc.category_level.caption_column</code>.
	 */
	public java.lang.String getCaptionColumn() {
		return (java.lang.String) getValue(7);
	}

	/**
	 * Setter for <code>calc.category_level.caption</code>.
	 */
	public void setCaption(java.lang.String value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>calc.category_level.caption</code>.
	 */
	public java.lang.String getCaption() {
		return (java.lang.String) getValue(8);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record9 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row9<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row9) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row9<java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row9) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.HIERARCHY_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field4() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.CODE_COLUMN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field5() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.RANK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field6() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.TABLE_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field7() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.ID_COLUMN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field8() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.CAPTION_COLUMN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field9() {
		return org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL.CAPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value2() {
		return getHierarchyId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value4() {
		return getCodeColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value5() {
		return getRank();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value6() {
		return getTableName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value7() {
		return getIdColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value8() {
		return getCaptionColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value9() {
		return getCaption();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value2(java.lang.Integer value) {
		setHierarchyId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value3(java.lang.String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value4(java.lang.String value) {
		setCodeColumn(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value5(java.lang.Integer value) {
		setRank(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value6(java.lang.String value) {
		setTableName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value7(java.lang.String value) {
		setIdColumn(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value8(java.lang.String value) {
		setCaptionColumn(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord value9(java.lang.String value) {
		setCaption(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoryLevelRecord values(java.lang.Integer value1, java.lang.Integer value2, java.lang.String value3, java.lang.String value4, java.lang.Integer value5, java.lang.String value6, java.lang.String value7, java.lang.String value8, java.lang.String value9) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached CategoryLevelRecord
	 */
	public CategoryLevelRecord() {
		super(org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL);
	}

	/**
	 * Create a detached, initialised CategoryLevelRecord
	 */
	public CategoryLevelRecord(java.lang.Integer id, java.lang.Integer hierarchyId, java.lang.String name, java.lang.String codeColumn, java.lang.Integer rank, java.lang.String tableName, java.lang.String idColumn, java.lang.String captionColumn, java.lang.String caption) {
		super(org.openforis.calc.persistence.jooq.tables.CategoryLevelTable.CATEGORY_LEVEL);

		setValue(0, id);
		setValue(1, hierarchyId);
		setValue(2, name);
		setValue(3, codeColumn);
		setValue(4, rank);
		setValue(5, tableName);
		setValue(6, idColumn);
		setValue(7, captionColumn);
		setValue(8, caption);
	}
}

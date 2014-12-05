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
public class WorkspaceSettingsRecord extends org.jooq.impl.UpdatableRecordImpl<org.openforis.calc.persistence.jooq.tables.records.WorkspaceSettingsRecord> implements org.jooq.Record3<java.lang.Long, java.lang.Long, org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS> {

	private static final long serialVersionUID = 1120766133;

	/**
	 * Setter for <code>calc.workspace_settings.id</code>.
	 */
	public void setId(java.lang.Long value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>calc.workspace_settings.id</code>.
	 */
	public java.lang.Long getId() {
		return (java.lang.Long) getValue(0);
	}

	/**
	 * Setter for <code>calc.workspace_settings.workspace_id</code>.
	 */
	public void setWorkspaceId(java.lang.Long value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>calc.workspace_settings.workspace_id</code>.
	 */
	public java.lang.Long getWorkspaceId() {
		return (java.lang.Long) getValue(1);
	}

	/**
	 * Setter for <code>calc.workspace_settings.view_steps</code>.
	 */
	public void setViewSteps(org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>calc.workspace_settings.view_steps</code>.
	 */
	public org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS getViewSteps() {
		return (org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS) getValue(2);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Long> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record3 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Long, java.lang.Long, org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS> fieldsRow() {
		return (org.jooq.Row3) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row3<java.lang.Long, java.lang.Long, org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS> valuesRow() {
		return (org.jooq.Row3) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field1() {
		return org.openforis.calc.persistence.jooq.tables.WorkspaceSettingsTable.WORKSPACE_SETTINGS.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field2() {
		return org.openforis.calc.persistence.jooq.tables.WorkspaceSettingsTable.WORKSPACE_SETTINGS.WORKSPACE_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS> field3() {
		return org.openforis.calc.persistence.jooq.tables.WorkspaceSettingsTable.WORKSPACE_SETTINGS.VIEW_STEPS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value2() {
		return getWorkspaceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS value3() {
		return getViewSteps();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkspaceSettingsRecord value1(java.lang.Long value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkspaceSettingsRecord value2(java.lang.Long value) {
		setWorkspaceId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkspaceSettingsRecord value3(org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS value) {
		setViewSteps(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkspaceSettingsRecord values(java.lang.Long value1, java.lang.Long value2, org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS value3) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached WorkspaceSettingsRecord
	 */
	public WorkspaceSettingsRecord() {
		super(org.openforis.calc.persistence.jooq.tables.WorkspaceSettingsTable.WORKSPACE_SETTINGS);
	}

	/**
	 * Create a detached, initialised WorkspaceSettingsRecord
	 */
	public WorkspaceSettingsRecord(java.lang.Long id, java.lang.Long workspaceId, org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS viewSteps) {
		super(org.openforis.calc.persistence.jooq.tables.WorkspaceSettingsTable.WORKSPACE_SETTINGS);

		setValue(0, id);
		setValue(1, workspaceId);
		setValue(2, viewSteps);
	}
}
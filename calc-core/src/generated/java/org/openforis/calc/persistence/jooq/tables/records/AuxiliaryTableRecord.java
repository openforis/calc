/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.calc.persistence.jooq.tables.AuxiliaryTableTable;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AuxiliaryTableRecord extends UpdatableRecordImpl<AuxiliaryTableRecord> implements Record4<Long, Long, String, String> {

	private static final long serialVersionUID = 1501833545;

	/**
	 * Setter for <code>calc.auxiliary_table.id</code>.
	 */
	public void setId(Long value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>calc.auxiliary_table.id</code>.
	 */
	public Long getId() {
		return (Long) getValue(0);
	}

	/**
	 * Setter for <code>calc.auxiliary_table.workspace_id</code>.
	 */
	public void setWorkspaceId(Long value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>calc.auxiliary_table.workspace_id</code>.
	 */
	public Long getWorkspaceId() {
		return (Long) getValue(1);
	}

	/**
	 * Setter for <code>calc.auxiliary_table.schema</code>.
	 */
	public void setSchema(String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>calc.auxiliary_table.schema</code>.
	 */
	public String getSchema() {
		return (String) getValue(2);
	}

	/**
	 * Setter for <code>calc.auxiliary_table.name</code>.
	 */
	public void setName(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>calc.auxiliary_table.name</code>.
	 */
	public String getName() {
		return (String) getValue(3);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record1<Long> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record4 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<Long, Long, String, String> fieldsRow() {
		return (Row4) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<Long, Long, String, String> valuesRow() {
		return (Row4) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Long> field1() {
		return AuxiliaryTableTable.AUXILIARY_TABLE.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Long> field2() {
		return AuxiliaryTableTable.AUXILIARY_TABLE.WORKSPACE_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field3() {
		return AuxiliaryTableTable.AUXILIARY_TABLE.SCHEMA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field4() {
		return AuxiliaryTableTable.AUXILIARY_TABLE.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long value2() {
		return getWorkspaceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value3() {
		return getSchema();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value4() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuxiliaryTableRecord value1(Long value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuxiliaryTableRecord value2(Long value) {
		setWorkspaceId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuxiliaryTableRecord value3(String value) {
		setSchema(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuxiliaryTableRecord value4(String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuxiliaryTableRecord values(Long value1, Long value2, String value3, String value4) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached AuxiliaryTableRecord
	 */
	public AuxiliaryTableRecord() {
		super(AuxiliaryTableTable.AUXILIARY_TABLE);
	}

	/**
	 * Create a detached, initialised AuxiliaryTableRecord
	 */
	public AuxiliaryTableRecord(Long id, Long workspaceId, String schema, String name) {
		super(AuxiliaryTableTable.AUXILIARY_TABLE);

		setValue(0, id);
		setValue(1, workspaceId);
		setValue(2, schema);
		setValue(3, name);
	}
}

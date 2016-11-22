/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables;


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
import org.openforis.calc.persistence.jooq.tables.records.AuxiliaryTableRecord;


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
public class AuxiliaryTableTable extends TableImpl<AuxiliaryTableRecord> {

	private static final long serialVersionUID = 323536958;

	/**
	 * The reference instance of <code>calc.auxiliary_table</code>
	 */
	public static final AuxiliaryTableTable AUXILIARY_TABLE = new AuxiliaryTableTable();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<AuxiliaryTableRecord> getRecordType() {
		return AuxiliaryTableRecord.class;
	}

	/**
	 * The column <code>calc.auxiliary_table.id</code>.
	 */
	public final TableField<AuxiliaryTableRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>calc.auxiliary_table.workspace_id</code>.
	 */
	public final TableField<AuxiliaryTableRecord, Long> WORKSPACE_ID = createField("workspace_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>calc.auxiliary_table.schema</code>.
	 */
	public final TableField<AuxiliaryTableRecord, String> SCHEMA = createField("schema", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

	/**
	 * The column <code>calc.auxiliary_table.name</code>.
	 */
	public final TableField<AuxiliaryTableRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

	/**
	 * Create a <code>calc.auxiliary_table</code> table reference
	 */
	public AuxiliaryTableTable() {
		this("auxiliary_table", null);
	}

	/**
	 * Create an aliased <code>calc.auxiliary_table</code> table reference
	 */
	public AuxiliaryTableTable(String alias) {
		this(alias, AUXILIARY_TABLE);
	}

	private AuxiliaryTableTable(String alias, Table<AuxiliaryTableRecord> aliased) {
		this(alias, aliased, null);
	}

	private AuxiliaryTableTable(String alias, Table<AuxiliaryTableRecord> aliased, Field<?>[] parameters) {
		super(alias, CalcSchema.CALC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<AuxiliaryTableRecord, Long> getIdentity() {
		return Keys.IDENTITY_AUXILIARY_TABLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<AuxiliaryTableRecord> getPrimaryKey() {
		return Keys.AUXILIARY_TABLE_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<AuxiliaryTableRecord>> getKeys() {
		return Arrays.<UniqueKey<AuxiliaryTableRecord>>asList(Keys.AUXILIARY_TABLE_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<AuxiliaryTableRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<AuxiliaryTableRecord, ?>>asList(Keys.AUXILIARY_TABLE__AUXILIARYTABLE_WS_FK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuxiliaryTableTable as(String alias) {
		return new AuxiliaryTableTable(alias, this);
	}

	/**
	 * Rename this table
	 */
	public AuxiliaryTableTable rename(String name) {
		return new AuxiliaryTableTable(name, null);
	}
}
package org.openforis.calc.persistence.postgis;

import javax.sql.DataSource;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.Update;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;

/**
 * Simple PostreSQL query builder
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public final class Psql extends DefaultDSLContext {
	private static final long serialVersionUID = 1L;
	public static final Schema PUBLIC = DSL.schemaByName("public");

	public enum Privilege {
		ALL, SELECT;
		public String toString() {
			return name().toLowerCase();
		};
	};

	public Psql() {
		super(SQLDialect.POSTGRES);
	}

	public Psql(DataSource dataSource) {
		super(dataSource, SQLDialect.POSTGRES);
//		super(DataSourceUtils.getConnection(dataSource), SQLDialect.POSTGRES);
		// TODO correctly implement transactions (CALC-125)
	}

	static String[] names(Field<?>[] fields) {
		String[] names = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			names[i] = fields[i].getName();
		}
		return names;
	}

	public GrantStep grant(Privilege... privileges) {
		return new GrantStep(this, privileges);
	}

	public CreateTableStep createTable(Table<?> table) {
		return new CreateTableStep(this, table);
	}

	public Select<?> selectStarFrom(Table<?> table) {		
		String tableName = table.getName();
		String schemaName = table.getSchema().getName();
		return select().from(DSL.tableByName(schemaName, tableName));
	}

	public AlterTableStep alterTable(Table<?> table) {
		return new AlterTableStep(this, table);
	}

	public CreateSchemaStep createSchema(Schema schema) {
		return new CreateSchemaStep(this, schema);
	}

	public DropSchemaStep dropSchemaIfExists(Schema schema) {
		return new DropSchemaStep(this, true, schema);
	}

	public SetDefaultSchemaSearchPathStep setDefaultSchemaSearchPath(Schema... schemas) {
		return new SetDefaultSchemaSearchPathStep(this, schemas);
	}

	public UpdateWithStep updateWith(Table<?> cursor, Update<?> update, Condition joinCondition) {
		return new UpdateWithStep(this, cursor, update, joinCondition);
	}
}

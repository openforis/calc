package org.openforis.calc.persistence.postgis;

import javax.sql.DataSource;

import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Simple PostreSQL query builder
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public final class Psql extends DefaultDSLContext {
	private static final long serialVersionUID = 1L;

	public enum Privilege {
		ALL, SELECT
	};

	public Psql() {
		super(SQLDialect.POSTGRES);
	}

	public Psql(DataSource dataSource) {
		super(DataSourceUtils.getConnection(dataSource), SQLDialect.POSTGRES);
	}

	public GrantStep grant(Privilege... privileges) {
		return new GrantStep(this, privileges);
	}

	public CreateTableStep createTable(Table<?> table) {
		return new CreateTableStep(this, table);
	}

	public Select<?> selectAll(Table<?> table) {		
		return select().from(DSL.tableByName(table.getSchema().getName(), table.getName()));
	}

	public AlterTableStep alterTable(Table<?> table) {
		return new AlterTableStep(this, table);
	}

	public static String[] names(Field<?>[] fields) {
		String[] names = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			names[i] = fields[i].getName();
		}
		return names;
	}
}

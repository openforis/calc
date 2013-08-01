package org.openforis.calc.persistence.postgis;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

/**
 * Simple PostreSQL query builder
 * 
 * @author G. Miceli
 *
 */
public final class Psql {
	private static final String SPACE = " ";
	private static final String DOUBLE_QUOTE = "\"";
	private static final String COMMA = ",";
	private static final String OPEN_PAREN = "(";
	private static final String CLOSE_PAREN = ")";
	
	private static final String SET_SCHEMA_SEARCH_PATH = "set search_path to %s";
	private static final String ALTER_TABLE = "alter table %s";
	private static final String ADD_COLUMN = "add column %s %s";
	private static final String DROP_SCHEMA_IF_EXISTS_CASCADE = "drop schema if exists %s cascade";
	private static final String CREATE_SCHEMA = "create schema %s";
	private static final String WITH = "with %s as (%s)";
	private static final String SELECT = "select %s";
	private static final String FROM = "from %s";
	private static final String INNER_JOIN = "inner join %s on %s";
	private static final String UPDATE = "update %s";
	private static final String SET = "set %s";
	private static final String WHERE = "where %s";
	private static final String CREATE_TABLE = "create table %s";
	private static final String AS = "as %s";
	private static final String GRANT_ALL_ON_SCHEMA = "grant all on schema %s to %s";
	private static final String GRANT_ALL_ON_TABLES = "grant all privileges on all tables in schema %s to %s";
	private static final String ADD_PRIMARY_KEY = "add primary key (%s)";

	private StringBuilder sb;
	private JdbcTemplate jdbc;
	private Logger log;

	public static final String PUBLIC = "public";
	public static final String INTEGER = "integer";

	public Psql() {
		sb = new StringBuilder();
		log = LoggerFactory.getLogger(getClass());
	}
	
	public Psql(JdbcTemplate jdbc) {
		this();
		this.jdbc = jdbc;
	}
	
	public Psql createSchema(String schema) {
		return append(CREATE_SCHEMA, quoteIdentifiers(schema));
	}
	
	public Psql setSchemaSearchPath(String... schemas) {
		return append(SET_SCHEMA_SEARCH_PATH, quoteIdentifiers(schemas));
	}

	/**
	 * Comma-separated list of f table or column names, each element surrounded by double quotes
	 * @param identifier
	 * @return
	 */
	private static String quoteIdentifiers(String... identifiers) {
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < identifiers.length; i++) {
			if ( i > 0 ) {
				sb2.append(COMMA);
			}
			sb2.append(DOUBLE_QUOTE);
			sb2.append(identifiers[i]);
			sb2.append(DOUBLE_QUOTE);
		}
		return sb2.toString();
	}
	
	private static String join(Object... elements) {
		return StringUtils.join(elements, COMMA);
	}

	/**
	 * Converts a string to a valid PSQL identifier (i.e. table, column or schema name)
	 * by replacing unallowed characters with underscore ("_") and converting to lowercase
	 * @param str
	 * @return
	 */
	public static String toIdentifier(String str) {
		return str.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
	public void execute(final Object... args) {
		String sql = toString();
		if ( args.length == 0 ) {
			log.debug(sql+";");
		} else {
			log.debug(sql+"; -- Parameters: "+join(args)+"");			
		}
		jdbc.execute(sql, new PreparedStatementCallback<Boolean>() {
			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i+1, args[0]);
				}
				return ps.execute();
			}
		});
	}

	public Psql alterTable(String table) {
		return append(ALTER_TABLE, quoteIdentifiers(table));
	}

	public Psql addColumn(String name, String type) {
		return append(ADD_COLUMN, name, type);
	}
	
	public Psql with(String alias, Object select) {
		return append(WITH, alias, select);
	}
	
	private Psql append(String format, Object... args) {
		if ( sb.length() > 0 ) {
			sb.append(SPACE);
		}
//		Object[] strs = new String[args.length];
//		for (int i = 0; i < args.length; i++) {
//			if ( args[i] instanceof Psql ) {
//				strs[i] = OPEN_PAREN + args[i] + CLOSE_PAREN;
//			} else {
//				strs[i] = String.valueOf(args[i]);
//			}
//		}
		String sql = String.format(format, args);
		sb.append(sql);
		return this;
	}

	public Psql select(Object... elements) {
		return append(SELECT, join(elements));
	}

	public Psql from(Object... elements) {
		return append(FROM, join(elements));
	}

	public Psql innerJoin(String table, Object condition) {
		return append(INNER_JOIN, quoteIdentifiers(table), condition);
	}

	public Psql update(String table) {
		return append(UPDATE, quoteIdentifiers(table));
	}

	public Psql set(Object... elements) {
		return append(SET, join(elements));
	}

	public Psql where(Object condition) {
		return append(WHERE, condition);
	}

	public Psql dropSchemaIfExistsCascade(String schema) {
		return append(DROP_SCHEMA_IF_EXISTS_CASCADE, quoteIdentifiers(schema));
	}
	
	public Psql createTable(String table) {
		return append(CREATE_TABLE, quoteIdentifiers(table));
	}

	public Psql as(Object expression) {
		return append(AS, expression);
	}

	public Psql grantAllOnTables(String schema, String user) {
		return append(GRANT_ALL_ON_TABLES, schema, user);
	}

	public Psql grantAllOnSchema(String schema, String user) {
		return append(GRANT_ALL_ON_SCHEMA, schema, user);
	}

	public Psql addPrimaryKey(String... columns) {
		return append(ADD_PRIMARY_KEY, quoteIdentifiers(columns));
	}
}

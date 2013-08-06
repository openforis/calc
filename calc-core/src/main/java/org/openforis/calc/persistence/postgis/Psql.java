package org.openforis.calc.persistence.postgis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Simple PostreSQL query builder
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public final class Psql {
	private static final String SPACE = " ";
	private static final String COMMA = ",";
	private static final String PAREN_FORMAT = "(%s)";
	private static final String QUOTE_FORMAT = "\"%s\"";
	
	private static final String SET_SCHEMA_SEARCH_PATH = "set search_path to %s";
	private static final String ALTER_TABLE = "alter table %s";
	private static final String ADD_COLUMN = "add column %s %s";
	private static final String DROP_SCHEMA_IF_EXISTS_CASCADE = "drop schema if exists %s cascade";
	private static final String CREATE_SCHEMA = "create schema %s";
	private static final String WITH = "with %s as (%s)";
	private static final String SELECT = "select %s";
	private static final String FROM = "from %s";
	private static final String INNER_JOIN = "inner join %s";
	private static final String ON = "on %s";
	private static final String UPDATE = "update %s";
	private static final String SET = "set %s";
	private static final String WHERE = "where %s";
	private static final String AND = "and %s";
	private static final String CREATE_TABLE = "create table %s";
	private static final String AS = "as %s";
	private static final String GRANT_ALL_ON_SCHEMA = "grant all on schema %s to %s";
	private static final String GRANT_ALL_ON_TABLES = "grant all privileges on all tables in schema %s to %s";
	private static final String ADD_PRIMARY_KEY = "add primary key (%s)";
	private static final String DELETE_FROM = "delete from %s";
	private static final String SELECT_EXISTS = "select exists (%s)";
	private static final String INSERT_INTO_WITH_COLS = "insert into %s (%s)";
	private static final String INSERT_INTO = "insert into %s";
	private static final String GROUP_BY = "group by %s";
	
	private StringBuilder sb;
	private JdbcTemplate jdbc;
	private Logger log;

	public static final String PUBLIC = "public";
	public static final String INTEGER = "integer";
	public static final String VARCHAR = "varchar";
	public static final String FLOAT8 = "float8";
	public static final String POINT4326 = "Geometry(Point,4326)";

	public Psql() {
		sb = new StringBuilder();
		log = LoggerFactory.getLogger(getClass());
	}
	
	public Psql(JdbcTemplate jdbc) {
		this();
		this.jdbc = jdbc;
	}
	
	private Psql append(String format, String... args) {
		return append(format, (Object[]) args);
	}
	
	private Psql append(String format, Object... args) {
		if ( sb.length() > 0 ) {
			sb.append(SPACE);
		}
		String sql = String.format(format, args);
		sb.append(sql);
		return this;
	}
	
	public Psql createSchema(String schema) {
		return append(CREATE_SCHEMA, schema);
	}
	
	public Psql setSchemaSearchPath(String... schemas) {
		return append(SET_SCHEMA_SEARCH_PATH, join(schemas));
	}

	/**
	 * Quotes a series of table or column name with double quotes,
	 * separating multiple items with commas
	 * @param identifier
	 * @return
	 */
	public static String quote(String... identifiers) {
		Object[] quoted = new Object[identifiers.length];
		for (int i = 0; i < quoted.length; i++) {
			quoted[i] = String.format(QUOTE_FORMAT, identifiers[i]);
		}
		return join(quoted);
	}

	private static String join(String... elements) {
		return join((Object[]) elements);
	}
	
	private static String join(Object... elements) {
		return StringUtils.join(elements, COMMA);
	}

	@Override
	public String toString() {
		return sb.toString();
	}
	
	public Boolean queryForBoolean(final Object... args) {
		Boolean result = queryForObject(new ResultSetExtractor<Boolean>() {
			@Override
			public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
				rs.next();
				boolean result = rs.getBoolean(1);
				return result;
			}
		}, args);
		
		return result;
	}
	
	private <T> T queryForObject(ResultSetExtractor<T> resultSetExtractor, final Object... args) {
		String sql = toString();
		if ( args.length == 0 ) {
			log.debug(sql+";");
		} else {
			log.debug(sql+"; -- Parameters: "+join(args)+"");			
		}
		
		T result = jdbc.query(sql, resultSetExtractor, args);
		
		return result;
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
		return append(ALTER_TABLE, table);
	}

	public Psql addColumn(String name, String type) {
		return append(ADD_COLUMN, name, type);
	}

	public Psql addColumn(String name, String type, int n) {
		String typeN = type + String.format(PAREN_FORMAT, n);
		return append(ADD_COLUMN, name, typeN);
	}
	
	public Psql with(String alias, Object select) {
		return append(WITH, alias, select);
	}

	public Psql select(Object... elements) {
		return append(SELECT, join(elements));
	}

	public Psql from(Object... elements) {
		return append(FROM, join(elements));
	}

	public Psql innerJoin(String table) {
		return append(INNER_JOIN, table);
	}

	public Psql on(Object condition) {
		return append(ON, condition);		
	}
	
	public Psql update(String table) {
		return append(UPDATE, table);
	}

	public Psql set(Object... elements) {
		return append(SET, join(elements));
	}

	public Psql where(Object condition) {
		return append(WHERE, condition);
	}

	public Psql and(Object condition) {
		return append(AND, condition);
	}
	
	public Psql dropSchemaIfExistsCascade(String schema) {
		return append(DROP_SCHEMA_IF_EXISTS_CASCADE, schema);
	}
	
	public Psql createTable(String table) {
		return append(CREATE_TABLE, table);
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
		return append(ADD_PRIMARY_KEY, columns);
	}

	public Psql deleteFrom(String table) {
		return append(DELETE_FROM, table);
	}
	
	public Psql selectExists(Object select){
		return append(SELECT_EXISTS, select);
	}

	public Psql insertInto(String table, String... columns) {
		if ( columns != null ) {
			return append(INSERT_INTO_WITH_COLS, table, join(columns));
		} else {
			return append(INSERT_INTO, table);
		}
	}

	public Psql groupBy(Object... elements) {
		return append(GROUP_BY, join(elements));
	}
}

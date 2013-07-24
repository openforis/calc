package org.openforis.calc.persistence.sql;

/**
 * 
 * @author G. Miceli
 *
 */
// TODO rename to Psql
public final class Sql {
	private static final String DOUBLE_QUOTE = "\"";
	private static final String SET_SCHEMA_SQL = "SET SCHEMA search_path TO %s, public";

	private Sql() {
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
	
	/**
	 * Surround a table or column name with double quotes
	 * @param identifier
	 * @return
	 */
	public static String quoteIdentifier(String identifier) {
		return DOUBLE_QUOTE + identifier + DOUBLE_QUOTE;
	}

	public static String setSchema(String schema) {
		return String.format(SET_SCHEMA_SQL, quoteIdentifier(schema));
	}
}

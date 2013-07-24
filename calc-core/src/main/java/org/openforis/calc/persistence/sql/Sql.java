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
	
	public static String toIdentifier(String identifier) {
		return identifier.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
	}
	
	/**
	 * Surround a table or column name with double quotes
	 * @param token
	 * @return
	 */
	public static String quoteIdentifier(String token) {
		return DOUBLE_QUOTE + token + DOUBLE_QUOTE;
	}

	public static String setSchema(String schema) {
		return String.format(SET_SCHEMA_SQL, quoteIdentifier(schema));
	}
}

package org.openforis.calc.persistence.sql;

/**
 * 
 * @author G. Miceli
 *
 */
public final class Sql {
	private static final String DOUBLE_QUOTE = "\"";

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
}

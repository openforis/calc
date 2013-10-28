package org.jooq.impl;

import org.jooq.Table;

/**
 * @author G. Miceli
 */
public class TableAliasUtil {
	public static Table<?> getAliasedTable(Table<?> table) {
		TableAlias<?> alias = (TableAlias<?>) table;
		return alias.getAliasedTable();
	}
}

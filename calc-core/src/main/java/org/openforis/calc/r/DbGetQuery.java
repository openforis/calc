/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 * 
 */
public class DbGetQuery extends DbQuery {

	/**
	 * @param previous
	 * @param rFunction
	 * @param query
	 */
	public DbGetQuery(RScript previous, RVariable connection, Object query) {
		super(previous, "dbGetQuery", connection, query);
	}

}

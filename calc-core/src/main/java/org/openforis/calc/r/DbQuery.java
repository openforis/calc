/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbQuery extends RScript {
	
	DbQuery(RScript previous, String rFunction, RVariable connection, Object query) {
		super(previous);
		
		append(rFunction);
		append("(conn=");
		append(connection.toScript());
		append(", statement='");
		append(query.toString());
		append("')");
	}
	
}

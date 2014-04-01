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
		
		String statement = query.toString();
		statement = statement.replaceAll("'", "\\\\'");
		
		append(rFunction);
		append("(conn=");
		append(connection.toScript());
		append(", statement='");
		append( statement );
		append("')");
	}
	
}

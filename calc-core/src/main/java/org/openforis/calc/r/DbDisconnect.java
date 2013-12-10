/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbDisconnect extends RScript {
	
	DbDisconnect(RScript previous, RVariable connection) {
		super(previous);
		
		append("dbDisconnect(");
		append( connection.toScript() );
		append(")");
		
	}
	
}

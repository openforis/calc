/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 * 
 */
public class CheckError extends RScript {

	CheckError(RScript previous, RVariable variable, RVariable connection) {
		super(previous);
		append("checkError(");
		append(variable.toScript());
		if ( connection != null ) {
			append(",");
			append(connection.toScript());
		}
		append(")");
	}

}

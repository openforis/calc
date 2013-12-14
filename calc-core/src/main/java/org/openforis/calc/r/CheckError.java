/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 * 
 */
public class CheckError extends RScript {

	protected CheckError(RScript previous, RVariable variable, RVariable connection) {
		super(previous);
		append("checkError(");
		append(variable.toScript());
		append(",");
		append(connection.toScript());
		append(")");
	}

}

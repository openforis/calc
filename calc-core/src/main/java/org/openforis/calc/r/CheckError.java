/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 * 
 */
public class CheckError extends RScript {

	protected CheckError(RScript previous, RVariable variable) {
		super(previous);
		append("checkError(");
		append(variable.toScript());
		append(")");
	}

}

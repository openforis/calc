/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class IsNa extends RScript {

	IsNa(RScript previous, RScript script) {
		super(previous);
		
		append("is.na(");
		append(script.toScript());
		append(")");
		
	}


}

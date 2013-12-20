/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class Not extends RScript {

	Not(RScript previous, RScript script) {
		super(previous);
		
		append(NOT);
		append(script.toScript());
		
	}


}

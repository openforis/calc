/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class Setwd extends RScript {

	Setwd(RScript previous, RScript script) {
		super(previous);
		
		append( "setwd('");
		append(script.toScript());
		append( "')");
	}


}

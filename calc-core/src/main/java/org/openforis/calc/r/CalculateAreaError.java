/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class CalculateAreaError extends RScript {
	
	CalculateAreaError( RScript previous, RVariable plots , RVariable strata ) {
		super(previous);
		
		append( "calculateAreaError" );
		append("(plots=");
		append( plots.toString() );
		append(", strata=");
		append( strata.toString() );
		append(")");
	}
	
}

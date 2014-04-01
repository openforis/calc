/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class CalculateQuantityError extends RScript {
	
	CalculateQuantityError(RScript previous, RVariable data, RVariable plots , RVariable strata ) {
		super(previous);
		
		append( "calculateQuantityError" );
		append("(data=");
		append( data.toScript() );
		append(", plots=");
		append( plots.toString() );
		append(", strata=");
		append( strata.toString() );
		append(")");
	}
	
}

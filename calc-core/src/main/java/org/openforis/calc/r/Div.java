/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class Div extends RScript {

	Div(RScript previous, RScript numerator, RScript denumenator) {
		super(previous);
		
		append("(");
		append(numerator.toScript());
		append( SPACE );
		append( "/" );
		append( SPACE );
		append("(");
		append(denumenator.toScript());
		append(")");
		append(")");
		
	}


}

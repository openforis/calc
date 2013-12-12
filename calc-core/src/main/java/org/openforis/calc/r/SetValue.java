/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class SetValue extends RScript {

	public SetValue(RScript previous, RVariable variable, RScript script) {
		super(previous);
		
		append(variable.toScript());
		append( SPACE );
		append( ASSIGN );
		append( SPACE );
		append(script.toScript());
	}


}

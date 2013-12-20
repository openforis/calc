/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class If extends RScript {

	If(RScript previous, RScript condition, RScript script) {
		super(previous);
		
		append("if(");
		append( condition.toScript() );
		append(") { ");
		append(NEW_LINE);
		append(script.toScript());
		append(NEW_LINE);
		append("}");
		
	}


}

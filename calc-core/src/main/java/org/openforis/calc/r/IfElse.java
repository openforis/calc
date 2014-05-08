/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class IfElse extends RScript {

	IfElse( RScript previous, RScript condition, RScript leftValue , RScript rightValue ) {
		super(previous);
		
		append("ifelse(");
		append( condition.toScript() );
		append(" , ");
		append(NEW_LINE);
		append( leftValue.toScript() );
		append(" , ");
		append( rightValue.toScript() );
		append(NEW_LINE);
		append(") ");
		
	}


}

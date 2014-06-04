/**
 * 
 */
package org.openforis.calc.r;


/**
 * @author Mino Togna
 *
 */
public class Sqldf extends RScript {
	
	Sqldf(RScript previous, String script) {
		super(previous);
		append("sqldf('");
		append( RScript.NEW_LINE );
		append( script );
		append( RScript.NEW_LINE );
		append("')");
	}
	
}

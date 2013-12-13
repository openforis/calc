/**
 * 
 */
package org.openforis.calc.r;


/**
 * @author Mino Togna
 *
 */
public class Try extends RScript {
	
	protected Try(RScript previous, RScript... scripts) {
		super(previous);
		append("try({");
		for (RScript script : scripts) {
			if(script != null){
				append( script.toString() );
			}
		}
		append("})");
	}
	
}

/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 * 
 */
public class RComment extends RScript {

	RComment(RScript previous, RScript script) {
		super(previous);
		append("#");
		append(SPACE);
		append(script.toScript());
	}

}

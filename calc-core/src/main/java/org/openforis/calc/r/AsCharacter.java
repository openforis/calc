/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class AsCharacter extends RScript {

	/**
	 * @param previous
	 */
	AsCharacter(RScript previous, RScript script) {
		super(previous);
		append("as.character(");
		append(script.toScript());
		append(")");
	}

}

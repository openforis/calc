/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class Source extends RScript {

	/**
	 * @param previous
	 */
	Source(RScript previous, String fileName) {
		super(previous);
		append("source(\"");
		append(fileName);
		append("\")");
	}

}

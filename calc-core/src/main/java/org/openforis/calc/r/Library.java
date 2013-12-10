/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class Library extends RScript {

	public Library(RScript previous, String name) {
		super(previous);
		
		append("library");
		append("(");
		append( escape(name) );
		append(")");
		
	}


}

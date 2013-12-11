/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbDriver extends RScript {

	public DbDriver(RScript previous, String name) {
		super(previous);
		
		append("dbDriver");
		append("(");
		append( escape(name) );
		append(")");
		
	}

}

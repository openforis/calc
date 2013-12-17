/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbRemoveTable extends RScript {

	DbRemoveTable(RScript previous, RVariable connection, String name) {
		super(previous);
		
		append("dbRemoveTable");
		append("(");
		append(connection.toScript());
		append(COMMA);
		append( escape(name) );
		append(")");
		
	}


}

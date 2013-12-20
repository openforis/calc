/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbExistsTable extends RScript {

	DbExistsTable(RScript previous, RVariable connection, String name) {
		super(previous);
		
		append("dbExistsTable");
		append("(");
		append(connection.toScript());
		append(COMMA);
		append( escape(name) );
		append(")");
		
	}


}

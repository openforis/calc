/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbWriteTable extends RScript {

	public DbWriteTable(RScript previous, RVariable connection, String name, RVariable variable) {
		super(previous);
		
		append("dbWriteTable");
		append("(");
		append(connection.toScript());
		append(COMMA);
		append( escape(name) );
		append(COMMA);
		append( variable.toScript() );
		append(COMMA);
		append("row.names=F");
		append(")");
		
	}


}

/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbQuoteString extends RScript {
	
	/**
	 * 
	 * @param previous script 
	 * @param conn A subclass of DBIConnection, representing an active connection to an DBMS.
	 * @param x A character vector to label as being escaped SQL
	 */
	DbQuoteString(RScript previous, RVariable conn , RVariable x ) {
		super(previous);
		
		append("dbQuoteString");
		append("( conn = ");
		append( conn.toScript() );
		append(" , x = ");
		append( x.toScript() );
		append(" )");
		
	}

}

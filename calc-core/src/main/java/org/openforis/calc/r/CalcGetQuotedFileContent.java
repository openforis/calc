/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class CalcGetQuotedFileContent extends RScript {
	
	/**
	 * 
	 * @param previous script 
	 * @param conn A subclass of DBIConnection, representing an active connection to an DBMS.
	 * @param x A character vector to label as being escaped SQL
	 */
	CalcGetQuotedFileContent(RScript previous, RVariable fileName ) {
		super(previous);
		
		append("calc.getQuotedFileContent( ");
		append( fileName.toScript() );
		append(" )");
		
	}

}

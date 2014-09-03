/**
 * 
 */
package org.openforis.calc.r;


/**
 * @author Mino Togna
 *
 */
public class Sqldf extends RVariable {
	
	Sqldf(RScript previous, String script) {
		super( previous , sqldfString(script) );
	}

	private static String sqldfString( String string ){
		StringBuilder sb = new StringBuilder();
		sb.append("sqldf('");
		sb.append( RScript.NEW_LINE );
		sb.append( string );
		sb.append( RScript.NEW_LINE );
		sb.append("')");
		
		return sb.toString();
	}
	
}

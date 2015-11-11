/**
 * 
 */
package org.openforis.calc.r;


/**
 * @author Mino Togna
 * 
 */
public class ReadChar extends RScript {

	ReadChar(RScript previous, RVariable con, RVariable nchars) {
		super(previous);
		append("readChar(");
		append( SPACE );
		append( con.toScript() );
		append( SPACE );
		append( ",");
		append( SPACE );
		append( nchars.toScript() );
		append(")");
	}

}

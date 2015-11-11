/**
 * 
 */
package org.openforis.calc.r;


/**
 * @author Mino Togna
 * 
 */
public class FileInfo extends RScript {

	FileInfo(RScript previous, RVariable rVariable) {
		super(previous);
		append("file.info(");
		append( SPACE );
		append( rVariable.toScript() );
		append( SPACE );
		append(")");
	}

}

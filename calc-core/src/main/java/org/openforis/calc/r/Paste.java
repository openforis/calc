/**
 * 
 */
package org.openforis.calc.r;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Mino Togna
 * 
 */
public class Paste extends RScript {

	Paste(RScript previous, RScript variable1, RScript variable2, String sep) {
		super(previous);
		append("paste(");
		append( variable1.toScript() );
		append( SPACE );
		append( ",");
		append( SPACE );
		append( variable2.toScript() );
		if ( StringUtils.isNotEmpty(sep) ){
			append( SPACE );
			append( ",");
			append( SPACE );
			append("sep = ");
			append( sep );
		}
		append(")");
	}

}

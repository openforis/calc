/**
 * 
 */
package org.openforis.calc.chain.export;

import org.openforis.calc.r.RScript;

/**
 * @author M. Togna
 *
 */
public class CalcROutputScript extends ROutputScript {

	/**
	 * @param fileName
	 * @param rScript
	 * @param type
	 * @param index
	 */
	public CalcROutputScript( ) {
		super( "calc.R" , new RScript() );
		
		addScript( r().setWd(r().rScript(".")));
	}


	private void addScript(RScript script) {
		getRScript().addScript( script );
	}


	public void addScript( ROutputScript  script ){
		getRScript().addScript( r().source(script.getFileName()) );
	}
	
}

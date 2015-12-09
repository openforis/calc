/**
 * 
 */
package org.openforis.calc.chain.export;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.Source;

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
		Source source 		= r().source(script.getFileName());
		RScript rScript 	= source;
		if( script instanceof CalculationStepROutputScript ){
			CalculationStep step = ( (CalculationStepROutputScript) script).getCalculationStep();
			if ( !step.getActive() ){
				rScript = r().comment( source );
			}
		}
		getRScript().addScript( rScript );
	}
	
}

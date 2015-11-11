package org.openforis.calc.chain.export;

import org.openforis.calc.r.RScript;

/**
 * 
 * @author M. Togna
 *
 */
public class CalculationStepsPostExecutionROutputScript extends ROutputScript {

	public CalculationStepsPostExecutionROutputScript( int index, RScript rScript ) {
		super( "calculation-steps-post-exec.R", rScript, Type.SYSTEM, index );
	}

}

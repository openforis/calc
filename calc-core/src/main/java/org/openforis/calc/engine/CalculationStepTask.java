package org.openforis.calc.engine;

import org.openforis.calc.chain.CalculationStep;


/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class CalculationStepTask extends Task {

	private ParameterMap parameters;
	private CalculationStep calculationStep;

	protected final ParameterMap parameters() {
		return parameters;
	}

	public CalculationStep getCalculationStep() {
		return calculationStep;
	}

	void setCalculationStep(CalculationStep calculationStep) {
		this.calculationStep = calculationStep;
		
		ParameterMap parameterMap = calculationStep.parameters();
		if ( parameterMap == null ) {
			this.parameters = new ParameterHashMap();			
		} else {
			this.parameters = parameterMap.deepCopy();
		}
	}
}

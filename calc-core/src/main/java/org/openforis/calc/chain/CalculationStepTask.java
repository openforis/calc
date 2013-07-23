package org.openforis.calc.chain;

import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Task;


/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class CalculationStepTask extends Task {

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
		this.parameters = parameterMap.deepCopy();
	}
}

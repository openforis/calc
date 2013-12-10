/**
 * 
 */
package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.Div;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;

/**
 * @author Mino Togna
 *
 */
public class CalculationStepRTask extends CalcRTask {

	private CalculationStep calculationStep;
	private RVariable dataFrame;
	private RScript plotArea;
	
	private Set<String> outputVariables;
	
	/**
	 * @param rEnvironment
	 * @param name
	 */
	public CalculationStepRTask(REnvironment rEnvironment, RVariable dataFrame, CalculationStep calculationStep, RScript plotArea) {
		super( rEnvironment, calculationStep.getCaption() );
		this.dataFrame = dataFrame;
		this.plotArea = plotArea;
		this.outputVariables = new HashSet<String>();
		setCalculationStep(calculationStep);
	}
	
	public CalculationStepRTask(REnvironment rEnvironment, RVariable dataFrame, CalculationStep calculationStep) {
		this(rEnvironment, dataFrame, calculationStep, null);
	}

	private void setCalculationStep(CalculationStep calculationStep) {
		this.calculationStep = calculationStep;
		
		String script = replaceVariables( this.dataFrame, this.calculationStep.getScript() );
		String variableName = getOutputVariable().getName();
		RVariable outputVar = r().variable( dataFrame, variableName );
		//set output variable with calculation step script
		SetValue setOutputValue = r().setValue( outputVar, r().rScript(script) );
		addScript(setOutputValue);

		this.outputVariables.add(variableName);
		
		// check if per/ha script needs to be added
		QuantitativeVariable variablePerHa = getOutputVariable().getVariablePerHa();
		if( variablePerHa != null &&  this.plotArea != null ) {
			
			String variablePerHaName = variablePerHa.getName();
			RVariable outputVarPerHa = r().variable( dataFrame, variablePerHaName );
			//set output variable per ha as result of output variable / plot area
			Div valuePerHa = r().div(outputVar, plotArea);
			SetValue setOutputValuePerHa = r().setValue( outputVarPerHa, valuePerHa );
			addScript(setOutputValuePerHa);
			
			this.outputVariables.add( variablePerHaName );
		}
		// add scripts 
//		script = "data$"+step.getOutputVariable().getName() + " <- " + script +";";
//		scripts.add(script);
		
		
		
	}
	
	public QuantitativeVariable getOutputVariable() {
		return (QuantitativeVariable) calculationStep.getOutputVariable();
	}

	public Set<String> getInputVariables() {
		return calculationStep.getInputVariables();
	}
	
	public Set<String> getOutputVariables() {
		return outputVariables;
	}
	
}

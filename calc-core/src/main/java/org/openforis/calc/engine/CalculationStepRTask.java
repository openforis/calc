/**
 * 
 */
package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.QuantitativeVariable;
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
	private RVariable connection;

	/**
	 * @param rEnvironment
	 * @param name
	 */
	public CalculationStepRTask(CalculationStep calculationStep, REnvironment rEnvironment, RVariable connection, RVariable dataFrame, RScript plotArea) {
		super(rEnvironment, calculationStep.getCaption());
		this.connection = connection;
		this.dataFrame = dataFrame;
		this.plotArea = plotArea;
		this.outputVariables = new HashSet<String>();
		this.calculationStep = calculationStep;
		
		initScripts();
	}

//	public CalculationStepRTask(REnvironment rEnvironment, RVariable dataFrame, CalculationStep calculationStep) {
////		this(rEnvironment, dataFrame, calculationStep, null);
//	}

	private void initScripts() {
		RScript script = this.calculationStep.getRScript();
		SetValue setOutputValuePerHa = null;

		String variableName = getOutputVariable().getName();
		RVariable outputVar = r().variable(this.dataFrame, variableName);

		this.outputVariables.add(variableName);

		// check if per/ha script needs to be added
		QuantitativeVariable variablePerHa = getOutputVariable().getVariablePerHa();
		if (variablePerHa != null && this.plotArea != null) {

			String variablePerHaName = variablePerHa.getName();
			RVariable outputVarPerHa = r().variable(dataFrame, variablePerHaName);
			// set output variable per ha as result of output variable / plot
			// area
			Div valuePerHa = r().div(outputVar, plotArea);
			setOutputValuePerHa = r().setValue(outputVarPerHa, valuePerHa);

			this.outputVariables.add(variablePerHaName);
		}

		// assign the result of the scripts (surrounded by a try statement)
		// execution to the variable result
		RVariable result = r().variable("result");
		SetValue setValue = r().setValue(result, r().rTry(script, setOutputValuePerHa));

		addScript(setValue);
		addScript(r().checkError(result, connection));
	}

	public QuantitativeVariable getOutputVariable() {
		return (QuantitativeVariable) calculationStep.getOutputVariable();
	}

	public Set<String> getInputVariables() {
		return this.calculationStep.getRScript().getVariables();
	}

	public Set<String> getOutputVariables() {
		return outputVariables;
	}

}

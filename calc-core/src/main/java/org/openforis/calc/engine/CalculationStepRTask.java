/**
 * 
 */
package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.metadata.CategoricalVariable;
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
	private RVariable plotArea;

	private Set<String> outputVariables;
	// temp fix. 
	private Set<String> allOutputVariables;
	private RVariable connection;

	/**
	 * @param rEnvironment
	 * @param name
	 */
	public CalculationStepRTask(CalculationStep calculationStep, REnvironment rEnvironment, RVariable connection, RVariable dataFrame, RVariable plotAreaVariable) {
		super(rEnvironment, calculationStep.getCaption());
		this.connection = connection;
		this.dataFrame = dataFrame;
		this.plotArea = plotAreaVariable;
		this.outputVariables = new HashSet<String>();
		this.allOutputVariables = new HashSet<String>();
		this.calculationStep = calculationStep;
		
		initScripts();
	}

//	public CalculationStepRTask(REnvironment rEnvironment, RVariable dataFrame, CalculationStep calculationStep) {
////		this(rEnvironment, dataFrame, calculationStep, null);
//	}

	private void initScripts() {
		RScript script = this.calculationStep.getRScript();
		
		SetValue setOutputValuePerHa = null;

		Variable<?> outputVariable = getOutputVariable();
		String variableName = outputVariable.getName();
		RVariable outputVar = r().variable(this.dataFrame, variableName);

		this.outputVariables.add(variableName);
		this.allOutputVariables.add(variableName);
		if( outputVariable instanceof CategoricalVariable ){
			this.outputVariables.add(outputVariable.getInputCategoryIdColumn());
			this.allOutputVariables.add(outputVariable.getInputCategoryIdColumn());
		}
		
		// check if per/ha script needs to be added
		if ( this.plotArea != null && outputVariable instanceof QuantitativeVariable ) {
			String variablePerHaName = ( (QuantitativeVariable)outputVariable ).getVariablePerHaName();
			RVariable outputVarPerHa = r().variable(dataFrame, variablePerHaName);
			// set output variable per ha as result of output variable / plot
			// area
			Div valuePerHa = r().div(outputVar, plotArea);
			setOutputValuePerHa = r().setValue(outputVarPerHa, valuePerHa);

			this.allOutputVariables.add(variablePerHaName);
		}

		// assign the result of the scripts (surrounded by a try statement)
		// execution to the variable result
		RVariable result = r().variable("result");
		SetValue setValue = r().setValue(result, r().rTry(script, setOutputValuePerHa));

		addScript( r().rScript("# ==================== " + calculationStep.getCaption() + " ====================") );
//		addScript( r().rScript("# ==========" ) );
//		addScript( r().rScript("# " + calculationStep.getCaption() ) );
//		addScript( r().rScript("# ==========" ) );
		addScript(setValue);
		addScript( r().checkError(result, connection) );
	}

	public Variable<?> getOutputVariable() {
		return calculationStep.getOutputVariable();
	}

	public Set<String> getInputVariables() {
		Set<String> variables = this.calculationStep.getRScript().getVariables();
		if( calculationStep.getType() == Type.CATEGORY ){
			// add id column as well
			variables.add( getOutputVariable().getInputCategoryIdColumn() );
		}
		return variables;
	}

	public Set<String> getOutputVariables() {
		return outputVariables;
	}
	
	public Set<String> getAllOutputVariables() {
		return allOutputVariables;
	}

}

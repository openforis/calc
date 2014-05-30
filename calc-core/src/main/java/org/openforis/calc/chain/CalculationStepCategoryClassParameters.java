/**
 * 
 */
package org.openforis.calc.chain;

import org.json.simple.JSONObject;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Variable;

/**
 * Wrapper class for calculation step category type settings
 * 
 * @author Mino Togna
 * 
 */
public class CalculationStepCategoryClassParameters extends ParameterHashMap {

	private static final String RIGHT = "right";
	private static final String LEFT = "left";
	private static final String CONDITION = "condition";
	private static final String VARIABLE_ID = "variableId";
	private static final String CLASS_CODE = "classCode";
	private static final String CLASS_ID = "classId";

	private CalculationStep calculationStep;

	@SuppressWarnings("unchecked")
	public CalculationStepCategoryClassParameters(CalculationStep calculationStep , JSONObject jsonObject) {
		super( jsonObject );
		this.calculationStep = calculationStep;
	}
	
	public CalculationStepCategoryClassParameters(CalculationStep calculationStep) {
		this.calculationStep = calculationStep;
	}

	public Integer getClassId() {
		return getInteger(CLASS_ID);
	}

	public void setClassId(Integer classId) {
		setInteger(CLASS_ID, classId);
	}

	public String getClassCode() {
		return getString(CLASS_CODE);
	}

	public void setClassCode(String classCode) {
		setString(CLASS_CODE, classCode);
	}

	public Integer getVariableId() {
		return getInteger(VARIABLE_ID);
	}

	public void setVariableId(Integer variableId) {
		setInteger(VARIABLE_ID, variableId);
	}

	public String getCondition() {
		return getString(CONDITION);
	}

	public void setCondition(String condition) {
		setString(CONDITION, condition);
	}

	public String getLeft() {
		return getString(LEFT);
	}

	public void setLeft(String left) {
		setString(LEFT, parseConditionValue(left));
	}

	public String getRight() {
		return getString(RIGHT);
	}

	public void setRight(String right) {
		setString(RIGHT, parseConditionValue(right));
	}

	private String parseConditionValue(String string) {
		Workspace ws = this.calculationStep.getWorkspace();
		Variable<?> classVariable = ws.getVariableById(getVariableId());

		if (classVariable instanceof CategoricalVariable<?> && !string.startsWith("'")) {
			string = "'" + string;
		}

		if (classVariable instanceof CategoricalVariable<?> && !string.endsWith("'")) {
			string += "'";
		}

		return string;
	}
}

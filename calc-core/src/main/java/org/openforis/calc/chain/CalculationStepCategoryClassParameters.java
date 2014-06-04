/**
 * 
 */
package org.openforis.calc.chain;

import org.openforis.calc.engine.ParameterHashMap;

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

	public CalculationStepCategoryClassParameters() {
		super();
	}
	
	public CalculationStepCategoryClassParameters(ParameterHashMap map) {
		super( map );
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
		setString(LEFT, left);
	}

	public String getRight() {
		return getString(RIGHT);
	}

	public void setRight(String right) {
		setString(RIGHT, right);
	}

}

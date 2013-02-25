package org.openforis.calc.model;

/**
 * 
 * @author G. Miceli
 *
 */
public interface NumericValue extends Value {
	Integer getVariableId();
	void setVariableId(Integer variableId);
	Double getValue();
	void setValue(Double value);
}

package org.openforis.calc.model;

/**
 * 
 * @author G. Miceli
 *
 */
public interface NumericValue extends Identifiable {
	Integer getValueId();
	void setValueId(Integer valueId);
	Integer getObservationId();
	void setObservationId(Integer obsId);
	Integer getVariableId();
	void setVariableId(Integer variableId);
	Double getValue();
	void setValue(Double value);
}

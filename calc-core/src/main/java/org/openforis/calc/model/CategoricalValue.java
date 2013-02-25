package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public interface CategoricalValue extends Identifiable{
	Integer getValueId();
	void setValueId(Integer valueId);
	Integer getObservationId();
	void setObservationId(Integer obsId);
	Integer getCategoryId();
	void setCategoryId(Integer categoryId);
}

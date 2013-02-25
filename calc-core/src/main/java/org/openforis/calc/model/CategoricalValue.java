package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public interface CategoricalValue extends Value {
	Integer getCategoryId();
	void setCategoryId(Integer categoryId);
}

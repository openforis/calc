package org.openforis.calc.metadata;

import javax.persistence.DiscriminatorValue;

/**
 * A variable which may take on a single numeric value.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@DiscriminatorValue("Q")
public class QuantitativeVariable extends Variable {
	private double defaultValue;
	private transient Unit<?> unit; //TODO map to column

	public void setUnit(Unit<?> unit) {
		this.unit = unit;
	}

	public Unit<?> getUnit() {
		return this.unit;
	}

	public void setDefaultValue(double defaultValue) {
		this.defaultValue = defaultValue;
	}

	public double getDefaultValue() {
		return this.defaultValue;
	}
}
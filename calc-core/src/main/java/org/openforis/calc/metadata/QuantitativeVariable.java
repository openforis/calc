package org.openforis.calc.metadata;

/**
 * A variable which may take on a single numeric value.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class QuantitativeVariable extends Variable {
	private double defaultValue;
	private Unit<?> unit;

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
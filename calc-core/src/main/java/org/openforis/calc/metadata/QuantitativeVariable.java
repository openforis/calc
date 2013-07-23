package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;

/**
 * A variable which may take on a single numeric value.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@DiscriminatorValue("Q")
public class QuantitativeVariable extends Variable {
	@Column(name = "default_value")
	private double defaultValue;
	
	@Transient //TODO map to column
	private transient Unit<?> unit; 

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
	
	@Override
	public Type getType() {
		return Type.QUANTITATIVE;
	}
}
package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;

import org.openforis.calc.metadata.Variable.Scale;

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
	private Double defaultValue;
	
	@Transient //TODO map to column
	private transient Unit<?> unit; 

	public void setUnit(Unit<?> unit) {
		this.unit = unit;
	}

	public Unit<?> getUnit() {
		return this.unit;
	}

	public void setDefaultValue(Double defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Double getDefaultValue() {
		return this.defaultValue;
	}
	
	@Override
	public Type getType() {
		return Type.QUANTITATIVE;
	}
	
	@Override
	public void setScale(Scale scale) {
		if ( scale != Scale.RATIO || scale != Scale.INTERVAL ) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
		super.setScale(scale);
	}

}
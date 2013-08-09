package org.openforis.calc.metadata;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
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
	private Double defaultValue;
	
	@Transient //TODO map to column
	private transient Unit<?> unit; 

	@OneToMany(mappedBy = "variable", fetch = FetchType.EAGER)
	private List<VariableAggregate> aggregates;

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
		if ( scale != Scale.RATIO && scale != Scale.INTERVAL ) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
		super.setScale(scale);
	}

	public List<VariableAggregate> getAggregates() {
		return aggregates;
	}

	public void setAggregates(List<VariableAggregate> aggregates) {
		this.aggregates = aggregates;
	}
}
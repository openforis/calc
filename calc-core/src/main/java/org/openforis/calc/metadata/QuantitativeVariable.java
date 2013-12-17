package org.openforis.calc.metadata;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A variable which may take on a single numeric value.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@DiscriminatorValue("Q")
public class QuantitativeVariable extends Variable<BigDecimal> {

	@JsonIgnore
	@Column(name = "default_value")
	private BigDecimal defaultValue;

	@JsonIgnore
	@Transient //TODO map to column
	private transient Unit<?> unit; 

	@OneToMany(mappedBy = "variable", fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	private List<VariableAggregate> aggregates;

	//variable_per_ha_id
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variable_per_ha_id")
	private QuantitativeVariable variablePerHa;
	
	@JsonIgnore
	@OneToOne(mappedBy = "variablePerHa")
//	@JoinColumn(name = "variable_per_ha_id")
	private QuantitativeVariable sourceVariable;
	
	public QuantitativeVariable getSourceVariable() {
		return sourceVariable;
	}
	
	public void setSourceVariable(QuantitativeVariable parentVariable) {
		this.sourceVariable = parentVariable;
	}
	
	public void setUnit(Unit<?> unit) {
		this.unit = unit;
	}
	
	@JsonIgnore
	public Unit<?> getUnit() {
		return this.unit;
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
		return CollectionUtils.unmodifiableList( aggregates );
	}

	public void setAggregates(List<VariableAggregate> aggregates) {
		this.aggregates = aggregates;
	}

	@Override
	public BigDecimal getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(BigDecimal defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public QuantitativeVariable getVariablePerHa() {
		return variablePerHa;
	}
	
	public void setVariablePerHa(QuantitativeVariable variablePerHa) {
		this.variablePerHa = variablePerHa;
	}
	
	public VariableAggregate getAggregate(String aggType) {
		for ( VariableAggregate agg : getAggregates() ) {
			if( agg.getAggregateType().equalsIgnoreCase(aggType) ) { 
				return agg;
			}
		}
		return null;
	}
	
	public boolean hasAggregate(String aggType) {
		VariableAggregate aggregate = getAggregate(aggType);
		return aggregate != null;
	}
	
	public void deleteAggregate(String agg) {
		if(this.hasAggregate(agg)){
			VariableAggregate aggregate = getAggregate(agg);
			this.aggregates.remove(aggregate);
		}
	}
	
	public String getVariablePerHaName(){
		return String.format( "%s_per_ha", getName() );
	}
	
	/**
	 * Returns the list of available aggregates for the variable
	 * @return
	 */
	@JsonInclude
	public VariableAggregate.AGGREGATE_TYPE[] getAggregateTypes() {
		VariableAggregate.AGGREGATE_TYPE[] aggTypes = { VariableAggregate.AGGREGATE_TYPE.SUM, VariableAggregate.AGGREGATE_TYPE.MAX , VariableAggregate.AGGREGATE_TYPE.MIN, VariableAggregate.AGGREGATE_TYPE.MEAN , VariableAggregate.AGGREGATE_TYPE.STDDEV };
		return aggTypes;
	}

	
}
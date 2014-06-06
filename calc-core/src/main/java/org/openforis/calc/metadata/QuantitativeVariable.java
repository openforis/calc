package org.openforis.calc.metadata;

import java.math.BigDecimal;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A variable which may take on a single numeric value.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class QuantitativeVariable extends Variable<BigDecimal> {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private BigDecimal defaultValue;

//	@Transient //TODO map to column
//	private transient Unit<?> unit; 

//	@OneToMany(mappedBy = "variable", fetch = FetchType.EAGER)
//	@Cascade(CascadeType.ALL)
	// TODO
	@JsonIgnore
	private List<VariableAggregate> aggregates;

	//variable_per_ha_id
//	@OneToOne(fetch = FetchType.EAGER)	
//	@JoinColumn(name = "variable_per_ha_id")
	// TODO 
	@JsonIgnore
	private QuantitativeVariable variablePerHa;
	
//	@OneToOne(mappedBy = "variablePerHa")
//	@JoinColumn(name = "variable_per_ha_id")
	// TODO
	@JsonIgnore
	private QuantitativeVariable sourceVariable;
	
	public QuantitativeVariable getSourceVariable() {
		return sourceVariable;
	}
	
	public void setSourceVariable(QuantitativeVariable parentVariable) {
		this.sourceVariable = parentVariable;
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
	@JsonIgnore
	public BigDecimal getDefaultValueTemp() {
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
	
	@JsonIgnore
	public String getVariablePerHaName(){
		return String.format( "%s_per_ha", getName() );
	}
	
	/**
	 * Returns the list of available aggregates for the variable
	 * @return
	 */
//	@JsonInclude
	@JsonIgnore
	public VariableAggregate.AGGREGATE_TYPE[] getAggregateTypes() {
		VariableAggregate.AGGREGATE_TYPE[] aggTypes = { VariableAggregate.AGGREGATE_TYPE.SUM, VariableAggregate.AGGREGATE_TYPE.MAX , VariableAggregate.AGGREGATE_TYPE.MIN, VariableAggregate.AGGREGATE_TYPE.MEAN , VariableAggregate.AGGREGATE_TYPE.STDDEV };
		return aggTypes;
	}

	
}
package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.NamedUserObject;

/**
 * 
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "variable_aggregate")
public class VariableAggregate extends NamedUserObject {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variable_id")
	private QuantitativeVariable variable;

	@Column(name = "aggregate_column")
	private String aggregateColumn;

	@Column(name = "aggregate_function")
	private String aggregateFunction;

	@Column(name = "aggregate_formula")
	private String aggregateFormula;

	public QuantitativeVariable getVariable() {
		return variable;
	}

	public void setVariable(QuantitativeVariable variable) {
		this.variable = variable;
	}

	public String getAggregateColumn() {
		return aggregateColumn;
	}

	public void setAggregateName(String aggregateName) {
		this.aggregateColumn = aggregateName;
	}

	/**
	 * Mondrian function used for aggregating across categories 
	 */
	public String getAggregateFunction() {
		return aggregateFunction;
	}

	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}

	/**
	 * Psql expression used for aggregating from single observations 
	 * to stratum/AOI level 
	 */
	public String getAggregateFormula() {
		return aggregateFormula;
	}

	public void setAggregateFormula(String aggregateFormula) {
		this.aggregateFormula = aggregateFormula;
	}

	public void setAggregateColumn(String aggregateColumn) {
		this.aggregateColumn = aggregateColumn;
	}
}

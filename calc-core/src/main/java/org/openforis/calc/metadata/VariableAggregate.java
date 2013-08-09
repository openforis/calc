package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.UserObject;

/**
 * 
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "variable_aggregate")
public class VariableAggregate extends UserObject {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variable_id")
	private Variable variable;

	@Column(name = "aggregate_column")
	private String aggregateColumn;

	@Column(name = "caption")
	private String caption;

	@Column(name = "aggregate_function")
	private String aggregateFunction;

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	public String getAggregateColumn() {
		return aggregateColumn;
	}

	public void setAggregateName(String aggregateName) {
		this.aggregateColumn = aggregateName;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getAggregateFunction() {
		return aggregateFunction;
	}

	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}

}

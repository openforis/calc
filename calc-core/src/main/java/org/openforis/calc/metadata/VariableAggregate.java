package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.common.NamedUserObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "variable_aggregate")
public class VariableAggregate extends NamedUserObject {

	public static enum AGGREGATE_TYPE {
		MIN, MAX, SUM, MEAN, STDDEV, AREA, PER_UNIT_AREA;

		public boolean equals(String other) {
			return this.toString().toLowerCase().equals(other);
		}

		public String toString() {
			return super.toString().toLowerCase();
		}

		public static boolean isValid(String agg) {
			agg = agg.toLowerCase();
			for( AGGREGATE_TYPE aggType : AGGREGATE_TYPE.values()) {
				if( aggType.equals(agg) ) {
					return true;
				}
			}
			return false;
		}
	}

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variable_id")
	private QuantitativeVariable variable;

	@JsonIgnore
	@Column(name = "aggregate_column")
	private String aggregateColumn;

	@Column(name = "aggregate_type")
	private String aggregateType;

	@JsonIgnore
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

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	/**
	 * Psql expression used for aggregating from single observations to stratum/AOI level
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

	/**
	 * Mondrian function used for aggregating across categories
	 */
	public String getAggregateFunction() {
		if ( AGGREGATE_TYPE.AREA.equals(aggregateType) ) {
			return AGGREGATE_TYPE.SUM.toString();
		} else {
			return aggregateType;
		}
	}

	/**
	 * Returns true in case this aggregate is used in a virtual cube (e.g. CalculatedMember) and the column in the output table doesn't exist
	 * 
	 * @return
	 */
	public boolean isVirtual() {
		return StringUtils.isBlank( getAggregateFormula() );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((aggregateType == null) ? 0 : aggregateType.hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableAggregate other = (VariableAggregate) obj;
		if (aggregateType == null) {
			if (other.aggregateType != null)
				return false;
		} else if (!aggregateType.equals(other.aggregateType))
			return false;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
	
	
}

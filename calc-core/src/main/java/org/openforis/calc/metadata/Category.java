package org.openforis.calc.metadata;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.UserObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents one possible value of a {@link CategoricalVariable}.
 * 
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@javax.persistence.Entity
@Table(name = "category")
public class Category extends UserObject {
	
	public static final BigDecimal TRUE_VALUE = BigDecimal.valueOf(1.0);
	public static final BigDecimal FALSE_VALUE = BigDecimal.valueOf(0.0);

	@Column(name = "code")
	private String code;
	
	@Column(name = "override")
	private boolean overrideInputMetadata;
	
	@Column(name = "sort_order")
	private int sortOrder;
	
	@Column(name = "original_id")
	private Integer originalId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variable_id")
	@JsonIgnore
	private Variable<?> variable;

	@Column(name = "value")
	private Double value;
	

	public Variable<?> getVariable() {
		return this.variable;
	}
	
	public void setVariable(Variable<?> variable) {
		this.variable = variable;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public void setOverrideInputMetadata(boolean overrideInputMetadata) {
		this.overrideInputMetadata = overrideInputMetadata;
	}

	public boolean isOverrideInputMetadata() {
		return this.overrideInputMetadata;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getSortOrder() {
		return this.sortOrder;
	}
	
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
package org.openforis.calc.metadata;

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
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "override")
	private boolean overrideInputMetadata;
	
	@Column(name = "sort_order") //TODO check column/variable name
	private int index;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variable_id")
	@JsonIgnore
	private Variable variable;

	public Variable getVariable() {
		return this.variable;
	}
	
	public void setVariable(Variable variable) {
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

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}
}
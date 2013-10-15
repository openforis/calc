/**
 * 
 */
package org.openforis.calc.web.form;


import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author S. Ricci
 * @author M. Togna
 * 
 */
public class CalculationStepForm {

	private Integer id;
	
	@NotEmpty
	private String name;

	@NotNull
	private Integer entityId;

	@NotNull
	private Integer variableId;

	@NotEmpty
	private String formula;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public Integer getVariableId() {
		return variableId;
	}

	public void setVariableId(Integer variableId) {
		this.variableId = variableId;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}
	
}

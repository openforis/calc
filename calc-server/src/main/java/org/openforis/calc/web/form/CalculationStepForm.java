/**
 * 
 */
package org.openforis.calc.web.form;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class CalculationStepForm {
	
	@NotEmpty
	private String name;
	
	@NotNull @Range(min=1)
	private Integer entityId;
	
	@NotNull @Range(min=1)
	private Integer variableId;

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

}

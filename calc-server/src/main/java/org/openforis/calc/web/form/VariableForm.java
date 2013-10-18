/**
 * 
 */
package org.openforis.calc.web.form;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author S. Ricci
 *
 */
public class VariableForm {

	@NotNull
	private Integer entityId;
	
	@NotEmpty
	private String name;

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

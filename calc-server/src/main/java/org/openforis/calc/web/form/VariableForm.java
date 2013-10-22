/**
 * 
 */
package org.openforis.calc.web.form;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.openforis.calc.web.form.validation.VariableName;

/**
 * @author S. Ricci
 *
 */
public class VariableForm {

	@NotNull
	private Integer entityId;
	
	@NotEmpty 
	@VariableName(message = "must be unique and only lowercase characters, numbers and underscores are allowed")
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

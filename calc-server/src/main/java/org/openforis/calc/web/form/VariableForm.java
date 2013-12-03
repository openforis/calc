/**
 * 
 */
package org.openforis.calc.web.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.openforis.calc.web.form.validation.VariableUniquenessConstraint;

/**
 * @author S. Ricci
 *
 */
@VariableUniquenessConstraint
public class VariableForm {

	private Integer id;
	
	@NotNull
	private Integer entityId;
	
	@NotEmpty
	@Pattern(regexp="[a-z][a-z0-9_]*", message="must contain only lowercase characters, numbers and underscores")
	private String name;

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
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

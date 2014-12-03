/**
 * 
 */
package org.openforis.calc.web.form;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.openforis.calc.web.form.validation.WorkspaceConstraint;

/**
 * @author Mino Togna
 */

@WorkspaceConstraint
public class WorkspaceForm {

	@NotEmpty
	@Pattern(regexp = "[a-z][a-z0-9_]*", message = "must contain only lowercase characters, numbers and underscores")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

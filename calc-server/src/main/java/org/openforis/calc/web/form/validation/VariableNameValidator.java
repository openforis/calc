/**
 * 
 */
package org.openforis.calc.web.form.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jooq.tools.StringUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class VariableNameValidator implements
		ConstraintValidator<VariableName, String> {

	private static final String VARIABLE_NAME_PATTERN = "[a-z][a-z0-9_]*";

	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	public void initialize(VariableName constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( StringUtils.isEmpty(value) ) {
			return true;
		} else {
			boolean valid = value.matches(VARIABLE_NAME_PATTERN);
			if ( valid ) {
				valid = isUnique(value);
			}
			return valid;
		}
	}
	
	private boolean isUnique(String name) {
		Workspace ws = workspaceService.getActiveWorkspace();
		Variable<?> v = ws.getVariableByName(name);
		return v == null;
	}

}

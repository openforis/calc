/**
 * 
 */
package org.openforis.calc.web.form.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.web.form.VariableForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class VariableValidator implements ConstraintValidator<VariableConstraint, VariableForm> {

	private static final String NAME_FIELD_NAME = "name";
	private static final String UNIQUE_NAME_MESSAGE = "must be unique";
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	public void initialize(VariableConstraint constraintAnnotation) {
	}

	@Override
	public boolean isValid(VariableForm value,
			ConstraintValidatorContext context) {
		boolean unique = isNameUnique(value.getId(), value.getName());
		if ( ! unique ) {
			((ConstraintValidatorContextImpl)context).buildConstraintViolationWithTemplate(UNIQUE_NAME_MESSAGE)
				.addPropertyNode(NAME_FIELD_NAME)
				.addConstraintViolation();
		}
		return unique;
	}

	private boolean isNameUnique(Integer id, String name) {
		Workspace ws = workspaceService.getActiveWorkspace();
		Variable<?> v = ws.getVariableByName(name);
		return v == null || v.getId().equals(id);
	}
}

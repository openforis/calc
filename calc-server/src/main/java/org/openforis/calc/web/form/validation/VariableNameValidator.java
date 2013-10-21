/**
 * 
 */
package org.openforis.calc.web.form.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author S. Ricci
 *
 */
public class VariableNameValidator implements
		ConstraintValidator<VariableName, String> {

	private static final String VARIABLE_NAME_PATTERN = "[a-z][a-z0-9_]*";

	@Override
	public void initialize(VariableName constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		} else {
			return value.matches(VARIABLE_NAME_PATTERN);
		}
	}

}

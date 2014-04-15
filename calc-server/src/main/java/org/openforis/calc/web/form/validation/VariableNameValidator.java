/**
 * 
 */
package org.openforis.calc.web.form.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.ArrayUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.web.form.VariableForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class VariableNameValidator implements ConstraintValidator<VariableNameConstraint, VariableForm> {

	private static final String FIELD_NAME = "name";
	private static final String[] RESERVED_WORDS = new String[] {
		"aoi", 
		"cluster", 
		"id", 
		"plot", 
		"plot_area",
		"stratum", 
		"weight"
		};
	
	private static final String RESERVED_WORD_MESSAGE = "is a reserved word and cannot be used as variable name.";
	private static final String NOT_UNIQUE_MESSAGE = "must be unique";
	
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	public void initialize(VariableNameConstraint constraintAnnotation) {
	}

	@Override
	public boolean isValid(VariableForm value,
			ConstraintValidatorContext context) {
		Integer variableId = value.getId();
		String name = value.getName();
		
		if ( isReservedWord(name) ) {
			addConstraintVialotion(context, RESERVED_WORD_MESSAGE);
			return false;
		} else if ( ! isUnique(variableId, name) ) {
			addConstraintVialotion(context, NOT_UNIQUE_MESSAGE);
			return false;
		} else {
			return true;
		}
	}

	private boolean isUnique(Integer id, String name) {
		Workspace ws = workspaceService.getActiveWorkspace();
		Variable<?> v = ws.getVariableByName(name);
		return v == null || v.getId().equals(id);
	}
	
	private boolean isReservedWord(String name) {
		return ArrayUtils.contains(RESERVED_WORDS, name);
	}
	
	private void addConstraintVialotion(ConstraintValidatorContext context, String message) {
		context.buildConstraintViolationWithTemplate(message)
			.addPropertyNode(FIELD_NAME)
			.addConstraintViolation();
	}
	
}

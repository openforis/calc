package org.openforis.calc.web.form.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.web.form.WorkspaceForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mino Togna
 *
 */
public class WorkspaceValidator implements ConstraintValidator<WorkspaceConstraint, WorkspaceForm> {
	
	private static final String FIELD_NAME = "name";

	private static final String NOT_UNIQUE_MESSAGE = "must be unique";
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	public void initialize(WorkspaceConstraint constraintAnnotation) {
	}

	@Override
	public boolean isValid(WorkspaceForm form, ConstraintValidatorContext context) {
		boolean valid = true;
		
		String workspaceName = form.getName();
		for (Workspace workspace : workspaceService.loadAllInfos()) {
			if( StringUtils.equalsIgnoreCase(workspaceName, workspace.getName()) ){
				context
					.buildConstraintViolationWithTemplate( NOT_UNIQUE_MESSAGE )
					.addPropertyNode( FIELD_NAME )
					.addConstraintViolation();
				
				valid = false;
				break;
			}
		}
		
		return valid;
	}
	
}

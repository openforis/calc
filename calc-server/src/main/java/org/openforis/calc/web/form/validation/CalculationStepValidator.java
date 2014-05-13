/**
 * 
 */
package org.openforis.calc.web.form.validation;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.web.form.CalculationStepForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 * @author Mino Togna
 */
public class CalculationStepValidator implements ConstraintValidator<CalculationStepContraint, CalculationStepForm> {

	private static final String IS_REQUIRED = " is required";
	private static final String CAPTION_FIELD_NAME = "caption";
	private static final String UNIQUE_CAPTION_MESSAGE = "must be unique";

	// added now for convenience. Mino
	@Autowired( required = true )
	private HttpServletRequest request;

	@Autowired
	private WorkspaceService workspaceService;

	@Override
	public void initialize(CalculationStepContraint constraintAnnotation) {
	}

	@Override
	public boolean isValid( CalculationStepForm form , ConstraintValidatorContext context ) {
		boolean valid = true;
		
		ConstraintValidatorContextImpl ctx = (ConstraintValidatorContextImpl) context;
		if ( !isCaptionUnique( form.getId(), form.getCaption() ) ) {
			ctx
				.buildConstraintViolationWithTemplate( UNIQUE_CAPTION_MESSAGE )
				.addPropertyNode( CAPTION_FIELD_NAME )
				.addConstraintViolation();
			
			valid = false;
		}
		
		Workspace ws = workspaceService.getActiveWorkspace();
		Type type = CalculationStep.Type.valueOf( form.getType() );
		
		switch (type) {
		
		case EQUATION :
			Integer listId = form.getEquationList();
			if( listId == null ) {
				ctx
				.buildConstraintViolationWithTemplate( IS_REQUIRED )
				.addPropertyNode( "equationList" )
				.addConstraintViolation();
				
				valid = false;
			} else {
				EquationList equationList = ws.getEquationListById( listId );
				if( equationList == null ) {
					ctx
					.buildConstraintViolationWithTemplate( "not found" )
					.addPropertyNode( "equationList" )
					.addConstraintViolation();
					
					valid = false;
				} else {
					
					Collection<String> equationVariables = equationList.getEquationVariables();
					for ( String equationVariable : equationVariables ) {
						String equationVarParam = request.getParameter(equationVariable);
						if( StringUtils.isBlank(equationVarParam) ){
//							ctx
//							.buildConstraintViolationWithTemplate( IS_REQUIRED )
//							.addPropertyNode( equationVariable )
//							.addConstraintViolation();
//							
							valid = false;
						} else {
							int variableId = Integer.parseInt( equationVarParam );
							Variable<?> variable = ws.getVariableById( variableId );
							if( variable == null ){
//								ctx
//								.buildConstraintViolationWithTemplate( "not found" )
//								.addPropertyNode( equationVariable )
//								.addConstraintViolation();
//								
								valid = false;
							}
						}	
					
					}
				}
			}
			// populate calc step parameters
			Integer codeVariable = form.getCodeVariable(); // request.getParameter( "codeVariable" );
			Variable<?> variable = ws.getVariableById( codeVariable );
			if( variable == null ){
				ctx
				.buildConstraintViolationWithTemplate( "not found" )
				.addPropertyNode( "codeVariable" )
				.addConstraintViolation();
				
				valid = false;
			}
			
			break;
			
			case SCRIPT:
				if( StringUtils.isBlank(form.getScript()) ) {
					ctx
					.buildConstraintViolationWithTemplate( IS_REQUIRED )
					.addPropertyNode( "script" )
					.addConstraintViolation();
					
					valid = false;
				}
				break;
		}
	
		return valid;
	}

	private boolean isCaptionUnique(Integer calculationStepId, String caption) {
		Workspace ws = workspaceService.getActiveWorkspace();
		ProcessingChain processingChain = ws.getDefaultProcessingChain();
		List<CalculationStep> steps = processingChain.getCalculationSteps();
		for (CalculationStep step : steps) {
			boolean sameStep = step.getId().equals(calculationStepId);
			if (!sameStep && step.getCaption().equals(caption)) {
				return false;
			}
		}
		return true;
	}

}

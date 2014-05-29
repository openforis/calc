/**
 * 
 */
package org.openforis.calc.web.form.validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.web.form.CalculationStepForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 * @author Mino Togna
 */
public class CalculationStepValidator implements ConstraintValidator<CalculationStepContraint, CalculationStepForm> {

	private static final String NOT_FOUND = "not found";
	private static final String IS_REQUIRED = " is required";
	private static final String UNIQUE_CAPTION_MESSAGE = "must be unique";

	@Autowired( required = true )
	private HttpServletRequest request;

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private CategoryManager categoryManager;
	
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
				.addPropertyNode( "caption" )
				.addConstraintViolation();
			
			valid = false;
		}
		
		Workspace ws = workspaceService.getActiveWorkspace();
		Type type = CalculationStep.Type.valueOf( form.getType() );
		
		switch (type) {

		case EQUATION:
			if (!validateTypeEquation(form, ctx, ws)) {
				valid = false;
			}
			break;
		case SCRIPT:
			if (!validateTypeScript(form, ctx, ws)) {
				valid = false;
			}
			break;
		case CATEGORY:

			if (!validateTypeCategory(form, valid, ctx, ws)) {
				valid = false;
			}
			break;
		}
	
		return valid;
	}

	private boolean validateTypeCategory(CalculationStepForm form, boolean valid, ConstraintValidatorContextImpl ctx, Workspace ws) {
		Integer categoryId = form.getCategoryId();
		Category category = ws.getCategoryById(categoryId);
		if( category == null ){
			ctx
			.buildConstraintViolationWithTemplate( NOT_FOUND )
			.addPropertyNode( "categoryId" )
			.addConstraintViolation();
			
			valid = false;
		} else {

			JSONArray categoryClasses = categoryManager.loadCategoryClasses( ws, categoryId );
			for (Object object : categoryClasses) {
				JSONObject o = (JSONObject) object;
				int classId = Integer.parseInt( o.get( "id" ).toString() );
				
				Integer variableId = form.getCategoryClassVariables().get( classId );
//				String fieldName = "categoryClassVariables['" + classId + "']";
				
				Variable<?> variable = ws.getVariableById(variableId);
				if( variable == null ){
					ctx
					.buildConstraintViolationWithTemplate( NOT_FOUND )
					.addPropertyNode( "categoryClassVariables['" + classId + "']" )
					.addConstraintViolation();
					
					valid = false;
				}
				
				String condition = form.getCategoryClassConditions().get(classId);
				if( StringUtils.isBlank(condition) ){
					ctx
					.buildConstraintViolationWithTemplate( NOT_FOUND )
					.addPropertyNode( "categoryClassConditions['" + classId + "']" )
					.addConstraintViolation();
					
					valid = false;
				}
				//TODO continue
//				switch ( this.condition ) {
//				case "BETWEEN":
//				case "NOT BETWEEN":
//				    if( this.value1 !== "" && this.value2 !== "" ) {
//						if( quantitative ){
//						    valid = $.isNumeric( this.value1 ) && $.isNumeric( this.value2 ); 
//						} 
//				    }  else {
//				    	valid = false;
//				    } 
//				    break;
//				case "IS NULL":
//				case "IS NOT NULL":
//				    valid = true;
//				    break;
//				default:
//				    if( this.value1 !== "" ) {
//					if( quantitative ){
//					    valid = $.isNumeric( this.value1 ); 
//					}
//				    }  else {
//					valid = false;
//				    } 
//				}
				
				
			}
		}
		
		
		return valid;
	}

	private boolean validateTypeEquation(CalculationStepForm form, ConstraintValidatorContextImpl ctx, Workspace ws) {
		boolean valid = true;
		if (!validateVariable(form, ctx, ws)) {
			valid = false;
		}
		
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
				.buildConstraintViolationWithTemplate( NOT_FOUND )
				.addPropertyNode( "equationList" )
				.addConstraintViolation();
				
				valid = false;
			} else {
				Map<String, Integer> eqVariablesParam = form.getEquationVariables();

				Collection<String> equationVariables = equationList.getEquationVariables();
				for ( String equationVariable : equationVariables ) {

					Integer variableId = eqVariablesParam.get(equationVariable);
					String eqationVariableInputName = "equationVariables['" + equationVariable + "']";
					if( variableId == null ){
						ctx
						.buildConstraintViolationWithTemplate( IS_REQUIRED )
						.addPropertyNode( eqationVariableInputName )
						.addConstraintViolation();
						
						valid = false;
					} else {
						Variable<?> variable = ws.getVariableById( variableId );
						if( variable == null ){
							ctx
							.buildConstraintViolationWithTemplate( NOT_FOUND )
							.addPropertyNode( eqationVariableInputName )
							.addConstraintViolation();

							valid = false;
						}
					}	
				
				}
			}
		}

		Integer codeVariable = form.getCodeVariable();
		Variable<?> variable = ws.getVariableById( codeVariable );
		if( variable == null ){
			ctx
			.buildConstraintViolationWithTemplate( NOT_FOUND )
			.addPropertyNode( "codeVariable" )
			.addConstraintViolation();
			
			valid = false;
		}
		return valid;
	}

	private boolean validateTypeScript(CalculationStepForm form, ConstraintValidatorContextImpl ctx, Workspace ws) {
		boolean valid = true;
		if (!validateVariable(form, ctx, ws)) {
			valid = false;
		}
		
		if( StringUtils.isBlank(form.getScript()) ) {
			ctx
			.buildConstraintViolationWithTemplate( IS_REQUIRED )
			.addPropertyNode( "script" )
			.addConstraintViolation();
			
			valid = false;
		}
		return valid;
	}

	private boolean validateVariable(CalculationStepForm form,  ConstraintValidatorContextImpl ctx, Workspace ws) {
		Integer variableId = form.getVariableId();
		Variable<?> variable = ws.getVariableById(variableId);
		if( variable == null ){
			ctx
			.buildConstraintViolationWithTemplate( NOT_FOUND )
			.addPropertyNode( "variableId" )
			.addConstraintViolation();
			
			return false;
		}
		return true;
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

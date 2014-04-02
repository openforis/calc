/**
 * 
 */
package org.openforis.calc.web.form.validation;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.SessionManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.web.form.CalculationStepForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class CalculationStepValidator implements ConstraintValidator<CalculationStepContraint, CalculationStepForm> {

	private static final String CAPTION_FIELD_NAME = "caption";
	private static final String UNIQUE_CAPTION_MESSAGE = "must be unique";
	
	@Autowired
	private SessionManager sessionManager;
	
	@Override
	public void initialize(CalculationStepContraint constraintAnnotation) {
	}

	@Override
	public boolean isValid(CalculationStepForm value,
			ConstraintValidatorContext context) {
		boolean unique = isCaptionUnique(value.getId(), value.getCaption());
		if ( ! unique ) {
			((ConstraintValidatorContextImpl)context).buildConstraintViolationWithTemplate(UNIQUE_CAPTION_MESSAGE)
				.addPropertyNode(CAPTION_FIELD_NAME)
				.addConstraintViolation();
		}
		return unique;
	}

	private boolean isCaptionUnique(Integer calculationStepId, String caption) {
		Workspace ws = sessionManager.getWorkspace();
		ProcessingChain processingChain = ws.getDefaultProcessingChain();
		List<CalculationStep> steps = processingChain.getCalculationSteps();
		for (CalculationStep step : steps) {
			boolean sameStep = step.getId().equals(calculationStepId);
			if ( ! sameStep && step.getCaption().equals(caption) ) {
				return false;
			}
		}
		return true;
	}

}

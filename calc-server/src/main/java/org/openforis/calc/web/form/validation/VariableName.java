package org.openforis.calc.web.form.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 
 * @author S. Ricci
 *
 */
@Documented
@Constraint(validatedBy = VariableNameValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface VariableName {

	String message() default "{VariableName}";
	
	Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

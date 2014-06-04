/**
 * 
 */
package org.openforis.calc.web.form.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.web.form.CategoryForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mino Togna
 */
public class CategoryValidator implements ConstraintValidator<CategoryContraint, CategoryForm> {

	private static final String IS_REQUIRED = " is required";
	private static final String UNIQUE_CAPTION_MESSAGE = "must be unique";

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private CategoryManager categoryManager;
	
	@Override
	public void initialize(CategoryContraint constraintAnnotation) {
	}

	@Override
	public boolean isValid( CategoryForm form, ConstraintValidatorContext ctx ){
		boolean valid = true;
		
		// caption must be unique
		Workspace ws = workspaceService.getActiveWorkspace();
		for ( Category category : ws.getCategories() ) {
			if( category.getCaption().equalsIgnoreCase( form.getCaption().trim() ) ){
				ctx
				.buildConstraintViolationWithTemplate( UNIQUE_CAPTION_MESSAGE )
				.addPropertyNode( "caption" )
				.addConstraintViolation();
			
			valid = false;
			}
		}
		
		int i = 0;
		for (String string : form.getCaptions()) {
			if( StringUtils.isBlank(string) ){
				ctx
				.buildConstraintViolationWithTemplate( IS_REQUIRED )
				.addPropertyNode( "captions[" + i + "]" )
				.addConstraintViolation();
				
				valid = false;
			}
			i++;
		}
		
		i = 0;
		for (String string : form.getCodes()) {
			if( StringUtils.isBlank(string) ){
				ctx
				.buildConstraintViolationWithTemplate( IS_REQUIRED )
				.addPropertyNode( "codes[" + i + "]" )
				.addConstraintViolation();
				
				valid = false;
			}
			i++;
		}
		
		return valid;
	}

}

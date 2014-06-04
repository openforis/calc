/**
 * 
 */
package org.openforis.calc.web.form;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.openforis.calc.web.form.validation.CategoryContraint;

/**
 * @author Mino Togna
 * 
 */
@CategoryContraint
public class CategoryForm {

	@NotEmpty
	private String caption;

	@NotEmpty
	private List<String> codes;

	@NotEmpty
	private List<String> captions;

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public List<String> getCodes() {
		return codes;
	}

	public void setCodes(List<String> codes) {
		this.codes = codes;
	}

	public List<String> getCaptions() {
		return captions;
	}

	public void setCaptions(List<String> captions) {
		this.captions = captions;
	}

}

/**
 * 
 */
package org.openforis.calc.web.form;



import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.openforis.calc.web.form.validation.CalculationStepContraint;

/**
 * @author S. Ricci
 * @author M. Togna
 * 
 */
@CalculationStepContraint
public class CalculationStepForm {

	private Integer id;
	
	@NotEmpty
	private String caption;

	@NotNull
	private Integer entityId;

	@NotNull
	private Integer variableId;

//	@NotEmpty
	private String script;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public Integer getVariableId() {
		return variableId;
	}

	public void setVariableId(Integer variableId) {
		this.variableId = variableId;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
}

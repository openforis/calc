/**
 * 
 */
package org.openforis.calc.web.form;



import java.util.Map;

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

	@NotEmpty
	private String type;
	
	private Integer equationList;
	
	private Integer codeVariable;
	
	private Map<String, Integer> equationVariables;
	
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getEquationList() {
		return equationList;
	}

	public void setEquationList(Integer equationList) {
		this.equationList = equationList;
	}

	public Integer getCodeVariable() {
		return codeVariable;
	}

	public void setCodeVariable(Integer codeVariable) {
		this.codeVariable = codeVariable;
	}

	public Map<String, Integer> getEquationVariables() {
		return equationVariables;
	}

	public void setEquationVariables(Map<String, Integer> equationVariables) {
		this.equationVariables = equationVariables;
	}
}

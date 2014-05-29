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
 * @author Mino Togna
 * 
 */
@CalculationStepContraint
public class CalculationStepForm {

	private Integer id;
	
	@NotEmpty
	private String caption;

	@NotNull
	private Integer entityId;

//	@NotNull
	private Integer variableId;

	private String script;

	@NotEmpty
	private String type;
	
	private Integer equationList;	
	private Integer codeVariable;	
	private Map<String, Integer> equationVariables;
	
	private Integer categoryId;
	private Map<String, Integer> categoryClassIds;
	private Map<String, Integer> categoryClassVariables;
	private Map<String, String> categoryClassConditions;
	private Map<String, Integer> categoryClassLeftConditions;
	private Map<String, Integer> categoryClassRightConditions;
	
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

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Map<String, Integer> getCategoryClassIds() {
		return categoryClassIds;
	}

	public void setCategoryClassIds(Map<String, Integer> categoryClassIds) {
		this.categoryClassIds = categoryClassIds;
	}

	public Map<String, Integer> getCategoryClassVariables() {
		return categoryClassVariables;
	}

	public void setCategoryClassVariables(Map<String, Integer> categoryClassVariables) {
		this.categoryClassVariables = categoryClassVariables;
	}

	public Map<String, String> getCategoryClassConditions() {
		return categoryClassConditions;
	}

	public void setCategoryClassConditions(Map<String, String> categoryClassConditions) {
		this.categoryClassConditions = categoryClassConditions;
	}

	public Map<String, Integer> getCategoryClassLeftConditions() {
		return categoryClassLeftConditions;
	}

	public void setCategoryClassLeftConditions(Map<String, Integer> categoryClassLeftConditions) {
		this.categoryClassLeftConditions = categoryClassLeftConditions;
	}

	public Map<String, Integer> getCategoryClassRightConditions() {
		return categoryClassRightConditions;
	}

	public void setCategoryClassRightConditions(Map<String, Integer> categoryClassRightConditions) {
		this.categoryClassRightConditions = categoryClassRightConditions;
	}
}

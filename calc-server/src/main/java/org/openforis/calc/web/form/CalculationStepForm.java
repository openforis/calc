/**
 * 
 */
package org.openforis.calc.web.form;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class CalculationStepForm {
	
	@NotEmpty
	private String name;
	
	private int variableId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVariableId() {
		return variableId;
	}

	public void setVariableId(int variableId) {
		this.variableId = variableId;
	}

}

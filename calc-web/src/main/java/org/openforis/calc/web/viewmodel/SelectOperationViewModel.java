/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import java.util.List;

import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * @author Mino Togna
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SelectOperationViewModel extends AbstractViewModel {

	private List<String> operation;

	private String selectedOperation;

	public List<String> getOperation() {
		return operation;
	}

	public void setOperation(List<String> operation) {
		this.operation = operation;
	}

	public String getSelectedOperation() {
		return selectedOperation;
	}

	public void setSelectedOperation(String selectedOperation) {
		this.selectedOperation = selectedOperation;
	}

}

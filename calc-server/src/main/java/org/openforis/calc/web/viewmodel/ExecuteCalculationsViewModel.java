/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import org.openforis.calc.operation.OperationsExecutor;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author Mino Togna
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ExecuteCalculationsViewModel {

	@WireVariable
	private OperationsExecutor operationsExecutor;

	protected String message;

	@Command("calc")
	@NotifyChange("message")
	public void execCalulations() {
		try {
			operationsExecutor.execute();
			message = "Calculations executed succefully";
		} catch ( Exception e ) {
			e.printStackTrace();
			message = e.getMessage();
		}
	}

	@Init
	public void init() {
		message = new String("");
	}

	public String getMessage() {
		return message;
	}

}

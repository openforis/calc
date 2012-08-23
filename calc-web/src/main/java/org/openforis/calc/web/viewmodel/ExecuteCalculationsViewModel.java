/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import org.openforis.calc.operation.OperationsExecutor;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Timer;

/**
 * @author Mino Togna
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ExecuteCalculationsViewModel extends AbstractViewModel {

	@WireVariable
	private OperationsExecutor operationsExecutor;

	protected String message;
	private Timer timer;
	private Progressmeter progressMeter;
	private Label progressMeterLabel;
	private Hbox progressBox;

	@Command("calc")
	@NotifyChange("message")
	public void execCalulations() {
		try {
			operationsExecutor.execute();

			startProgress();

		} catch ( Exception e ) {
			e.printStackTrace();
			timer.stop();
			throw new RuntimeException("Error while executing calculations", e);
		}
	}

	private void startProgress() {
		timer.start();

		progressBox.setVisible(true);
		progressMeter.setValue(0);
		progressMeterLabel.setValue(0 + "%");
	}

	@NotifyChange("operationsExecutor")
	@Command("updateStatus")
	public void onTimer() {

		if ( !operationsExecutor.isRunning() ) {
			timer.stop();
		}
		int completedPercentage = 100 * operationsExecutor.completedOperations() / operationsExecutor.getOperations().size();
		progressMeter.setValue(completedPercentage);
		progressMeterLabel.setValue(completedPercentage + "%");
	}

	@Init
	public void init(@ContextParam(ContextType.COMPONENT) Component component, @ContextParam(ContextType.VIEW) Component view) {
		message = new String("");

		timer = (Timer) view.getFellow("timer");
		progressMeter = (Progressmeter) view.getFellow("progressMeter");
		progressMeterLabel = (Label) view.getFellow("progressMeterLabel");
		progressBox = (Hbox) view.getFellow("progressBox");
	}

	public String getMessage() {
		return message;
	}

	public OperationsExecutor getOperationsExecutor() {
		return operationsExecutor;
	}

}

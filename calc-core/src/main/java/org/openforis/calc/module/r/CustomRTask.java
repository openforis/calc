package org.openforis.calc.module.r;

import org.openforis.calc.engine.CalculationStepTask;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Runs a user-defined R statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomRTask extends CalculationStepTask {

	@Autowired
	private R r;
	
	@Override
	synchronized
	protected void execute() throws RException {
		REnvironment env = r.newEnvironment();
		String script = getCalculationStep().getScript();
		log().debug("Custom R: "+script);
		env.eval(script);
	}
}
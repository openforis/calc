package org.openforis.calc.engine;

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
		ParameterMap params = parameters();
		String rScript = params.getString("r");
		log().debug("Custom R: "+rScript);
		env.eval(rScript);
	}
}
package org.openforis.calc.engine;

import java.sql.SQLException;

import org.openforis.calc.r.R;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Runs a user-defined R statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomRTask extends CalculationStepTask {

	@Override
	protected void execute() throws SQLException {
		ParameterMap params = parameters();
		String rScript = params.getString("r");
		Context ctx = getContext();
		R r = ctx.getR();
		log().info("Executing custom R: "+rScript);
		r.eval(rScript+"\n");
	}
}
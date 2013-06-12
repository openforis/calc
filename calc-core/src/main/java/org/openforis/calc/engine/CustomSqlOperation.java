package org.openforis.calc.engine;

import org.openforis.calc.engine.Task.Context;

/**
 * Defines an operation which runs a user-defined SQL statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomSqlOperation extends Operation {

	CustomSqlOperation(Module module) {
		super(module, "exec-sql");
	}

	@Override
	public Task createTask(Context context, Parameters params) {
		// TODO Auto-generated method stub
		return null;
	}
}
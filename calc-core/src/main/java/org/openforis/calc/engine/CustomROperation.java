package org.openforis.calc.engine;

import org.openforis.calc.engine.Task.Context;

/**
 * Defines an operation which runs a user-defined R statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomROperation extends Operation {

	CustomROperation(Module module) {
		super(module, "exec-r");
	}

	@Override
	public Task createTask(Context context, Parameters params) {
		// TODO Auto-generated method stub
		return null;
	}
}
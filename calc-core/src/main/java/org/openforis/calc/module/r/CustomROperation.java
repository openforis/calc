package org.openforis.calc.module.r;

import org.openforis.calc.module.Module;
import org.openforis.calc.module.Operation;


/**
 * Defines an operation which runs a user-defined R statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomROperation extends Operation<CustomRTask> {

	CustomROperation(Module module) {
		super(module, "exec-r");
	}
}
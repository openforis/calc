package org.openforis.calc.engine;


/**
 * Defines an operation which runs a user-defined SQL statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomSqlOperation extends Operation<CustomSqlTask> {

	CustomSqlOperation(Module module) {
		super(module, "exec-sql");
	}
}
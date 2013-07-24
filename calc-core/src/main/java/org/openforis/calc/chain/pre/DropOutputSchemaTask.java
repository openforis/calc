package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;

/**
 * Drops the output schema and all contained data.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class DropOutputSchemaTask extends Task {

	private void dropOutputSchema() {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		String sql = "DROP SCHEMA IF EXISTS %s CASCADE";
		executeSql(sql, outputSchema);
		log().info("Dropped output schema: " + outputSchema);
	}

	@Override
	protected void execute() throws Throwable {
		dropOutputSchema();
	}

}
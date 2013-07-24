package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drops the output schema and all contained data.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class DropOutputSchemaTask extends Task {

	Logger logger = LoggerFactory.getLogger(PrepareOutputSchemaTask.class);

	private void dropOutputSchema() {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		String sql = "DROP SCHEMA IF EXISTS %s CASCADE";
		executeSql(sql, outputSchema);
		logger.info("Dropped output schema %s", outputSchema);

	}

	@Override
	protected void execute() throws Throwable {
		dropOutputSchema();
	}

}
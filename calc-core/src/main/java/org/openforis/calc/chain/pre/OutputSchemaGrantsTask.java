package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.springframework.beans.factory.annotation.Value;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author G. Miceli
 */
public final class OutputSchemaGrantsTask extends SqlTask {

	@Value("${calc.jdbc.db}")
	private String systemUser;
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		// TODO The following as no effect!
		psql().grantAllOnTables(outputSchema, systemUser).execute();
		psql().grantAllOnSchema(outputSchema, systemUser).execute();
	}
}
package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Recreates the output schema and sets it as the default schema for operations.  
 * Fails if output schema already exists.
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz 
 */
public final class CreateOutputSchemaTask extends SqlTask {

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		psql().createSchema(outputSchema).execute();
		psql().setSchemaSearchPath(outputSchema, Psql.PUBLIC).execute();
	}
}
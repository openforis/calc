package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.OutputSchema;

/**
 * Drops the output schema and all contained data.
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz 
 */
public final class ResetOutputSchemaTask extends Task {

	@Override
	protected void execute() throws Throwable {
		OutputSchema outputSchema = getOutputSchema();
		// Drop old schema
		psql().dropSchemaIfExists(outputSchema).cascade().execute();
		// Create empty schema
		psql().createSchema(outputSchema).execute();
		// Grant access to calc system user
		psql().grant(Privilege.ALL).onSchema(outputSchema).to(getSystemUser()).execute();
	}
}
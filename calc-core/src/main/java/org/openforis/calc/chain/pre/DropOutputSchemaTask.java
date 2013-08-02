package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Drops the output schema and all contained data.
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz 
 */
public final class DropOutputSchemaTask extends SqlTask {

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = Psql.quote(workspace.getOutputSchema());
		psql().dropSchemaIfExistsCascade(outputSchema).execute();
	}

}
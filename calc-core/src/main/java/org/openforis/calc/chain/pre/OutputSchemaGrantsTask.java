package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.Psql;
import org.springframework.beans.factory.annotation.Value;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author G. Miceli
 */
public final class OutputSchemaGrantsTask extends SqlTask {

	@Value("${calc.jdbc.username}")
	private String systemUser;
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = Psql.quote(workspace.getOutputSchema());
		psql().grantAllOnTables(outputSchema, systemUser).execute();
		psql().grantAllOnSchema(outputSchema, systemUser).execute();
	}
}
package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.PsqlBuilder;
import org.springframework.beans.factory.annotation.Value;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author G. Miceli
 */
public final class OutputSchemaGrantsTask extends Task {

	@Value("${calc.jdbc.username}")
	private String systemUser;
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		String outputSchema = PsqlBuilder.quote(workspace.getOutputSchema());
		createPsqlBuilder().grantAllOnTables(outputSchema, systemUser).execute();
		createPsqlBuilder().grantAllOnSchema(outputSchema, systemUser).execute();
	}
}
package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.springframework.beans.factory.annotation.Value;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class SetOutputSchemaGrantsTask extends Task {

	@Value("${calc.jdbc.db}")
	private String systemUser;
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		// TODO The following as no effect! 
		executeSql("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA %s TO %s", outputSchema, systemUser);
	}
}
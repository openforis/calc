package org.openforis.calc.schema;

import org.openforis.calc.engine.Workspace;

/**
 * 
 * @author G. Miceli
 *
 */
public class Schemas {
	private InputSchema inputSchema;
	private OutputSchema outputSchema;
	private RolapSchema rolapSchema;
	
	public Schemas(Workspace workspace) {
		inputSchema = new InputSchema(workspace);
		outputSchema = new OutputSchema(workspace, inputSchema);
		rolapSchema = new RolapSchema(workspace, outputSchema);
	}

	public InputSchema getInputSchema() {
		return inputSchema;
	}

	public OutputSchema getOutputSchema() {
		return outputSchema;
	}

	public RolapSchema getRolapSchema() {
		return rolapSchema;
	}

	public Workspace getWorkspace() {
		return outputSchema.getWorkspace();
	}
}

/**
 * 
 */
package org.openforis.calc.rdb;

import org.openforis.calc.engine.Workspace;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class OutputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;
	private InputSchema inputSchema;
	
	public OutputSchema(Workspace workspace, InputSchema inputSchema) {
		super(workspace.getOutputSchema());
		this.workspace = workspace;
		this.inputSchema = inputSchema;
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	public InputSchema getInputSchema() {
		return inputSchema;
	}
}

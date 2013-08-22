/**
 * 
 */
package org.openforis.calc.rdb;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.rolap.StratumDimensionTable;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class OutputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;
	private InputSchema inputSchema;
	private StratumDimensionTable stratumDimensionTable;
	
	public OutputSchema(Workspace workspace, InputSchema inputSchema) {
		super(workspace.getOutputSchema());
		this.workspace = workspace;
		this.inputSchema = inputSchema;
		this.stratumDimensionTable = new StratumDimensionTable(this);
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	public InputSchema getInputSchema() {
		return inputSchema;
	}
	
	public StratumDimensionTable getStratumDimensionTable() {
		return stratumDimensionTable;
	}
}

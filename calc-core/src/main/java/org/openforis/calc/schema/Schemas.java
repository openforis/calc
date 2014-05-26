package org.openforis.calc.schema;

import org.openforis.calc.engine.Workspace;

/**
 * 
 * @author G. Miceli
 * @author Mino Togna 
 */
public class Schemas {
	private Workspace workspace;

	private DataSchema inputSchema;
	private ExtendedSchema extendedSchema;
	private RolapSchema rolapSchema;

	public Schemas(Workspace workspace) {
		this.workspace = workspace;

		inputSchema = new DataSchema(workspace);
		extendedSchema = new ExtendedSchema(workspace);
		rolapSchema = new RolapSchema(workspace, inputSchema, extendedSchema);
	}

	public DataSchema getDataSchema() {
		return inputSchema;
	}

	public ExtendedSchema getExtendedSchema() {
		return extendedSchema;
	}

	public RolapSchema getRolapSchema() {
		return rolapSchema;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}
}

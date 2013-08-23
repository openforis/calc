/**
 * 
 */
package org.openforis.calc.schema;

import org.openforis.calc.engine.Workspace;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class InputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;
	
	public InputSchema(Workspace workspace) {
		super(workspace.getInputSchema());
		this.workspace = workspace;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}

}

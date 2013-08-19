package org.openforis.calc.persistence.jooq;

import org.jooq.impl.SchemaImpl;
import org.openforis.calc.engine.Workspace;

/**
 * 
 * @author G. Miceli
 *
 */
public class OutputSchema extends SchemaImpl {

	private static final long serialVersionUID = 1L;
	private Workspace workspace;

	OutputSchema(Workspace workspace) {
		super(workspace.getOutputSchema());
		this.workspace = workspace;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
}

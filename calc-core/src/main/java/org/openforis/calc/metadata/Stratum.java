package org.openforis.calc.metadata;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.StratumBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a statum.
 * 
 * @author Mino Togna
 * @author S. Ricci
 */
public class Stratum extends StratumBase {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Workspace workspace;

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		setWorkspaceId( workspace.getId() );
	}
}

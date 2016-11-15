/**
 * 
 */
package org.openforis.calc.metadata;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.AuxiliaryTableBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author M. Togna
 *
 */
public class AuxiliaryTable extends AuxiliaryTableBase {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Workspace workspace;
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		setWorkspaceId( workspace.getId().longValue() );
	}
	
}

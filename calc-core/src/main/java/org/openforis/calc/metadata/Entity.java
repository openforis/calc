package org.openforis.calc.metadata;

import org.openforis.calc.workspace.Workspace;
import java.util.ArrayList;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.common.UserObject;

/**
 * Provides metadata about a particular unit of observation, calculation or analysis. Entities are anything which may have attributes for variables associated with it.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class Entity extends UserObject {
	private Workspace workspace;
	private String caption;
	private Entity parent;
	private ArrayList<Variable> variables = new ArrayList<Variable>();
	private DataTable dataTable;

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public DataTable getDataTable() {
		return this.dataTable;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public void setCaption(String caption) {
		this.caption = caption;
	}
}
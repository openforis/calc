package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.UserObject;
import org.openforis.calc.workspace.Workspace;

/**
 * Provides metadata about a particular unit of observation, calculation or
 * analysis. Entities are anything which may have attributes for variables
 * associated with it.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "entity")
public final class Entity extends UserObject {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	
	@Column(name = "caption")
	private String caption;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_entity_id")
	private Entity parent;
	
//	private ArrayList<Variable> variables = new ArrayList<Variable>();
//	private DataTable dataTable;

	public Workspace getWorkspace() {
		return this.workspace;
	}

//	public DataTable getDataTable() {
//		return this.dataTable;
//	}

	public String getCaption() {
		return caption;
	}

	@Column(name = "caption")
	public void setCaption(String caption) {
		this.caption = caption;
	}
}
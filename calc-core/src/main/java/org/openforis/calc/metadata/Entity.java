package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.openforis.calc.common.UserObject;
import org.openforis.calc.engine.Workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class Entity extends UserObject {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	@JsonIgnore
	private Workspace workspace;

	@Column(name = "data_table")
	private String table;

	@Column(name = "caption")
	private String caption;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_entity_id")
	private Entity parent;
	
	@Column(name = "sort_order")
	private int sortOrder;
	
	@OneToMany(mappedBy = "entity", fetch = FetchType.EAGER)
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT) 
	@Cascade(CascadeType.ALL)
	private List<Variable> variables = new ArrayList<Variable>();
	
	public Workspace getWorkspace() {
		return this.workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public String getDataTable() {
		return table;
	}

	public void setDataTable(String table) {
		this.table = table;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	public void addVariable(Variable variable) {
		variable.setEntity(this);
		variables.add(variable);
	}
	
	public List<Variable> getVariables() {
		return Collections.unmodifiableList(variables);
	}

	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity parent) {
		this.parent = parent;
	}
}
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
	private String dataTable;

	@Column(name = "caption")
	private String caption;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_entity_id")
	private Entity parent;
	
	@Column(name = "sort_order")
	private int sortOrder;
	
	@Column(name = "input")
	private boolean input;
	
	@Column(name = "override")
	private boolean override;
	
	@Column(name = "x_column")
	private String xColumn;
	
	@Column(name = "y_column")
	private String yColumn;
	
	@Column(name = "srs_column")
	private String srsColumn;
	
	@Column(name = "location_column")
	private String locationColumn;
	
	@Column(name = "cluster_column")
	private String clusterColumn;
	
	@Column(name = "unit_no_column")
	private String unitNoColumn;

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
		return dataTable;
	}

	public void setDataTable(String table) {
		this.dataTable = table;
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
	
	public int getSortOrder() {
		return sortOrder;
	}
	
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public void setInput(boolean input) {
		this.input = input;
	}
	
	public boolean isInput() {
		return input;
	}
	
	public void setOverride(boolean override) {
		this.override = override;
	}
	
	public boolean isOverride() {
		return override;
	}
	
	public void setXColumn(String xColumn) {
		this.xColumn = xColumn;
	}

	public String getXColumn() {
		return xColumn;
	}

	public void setYColumn(String yColumn) {
		this.yColumn = yColumn;
	}
	
	public String getYColumn() {
		return yColumn;
	}
	
	public void setSrsColumn(String srsColumn) {
		this.srsColumn = srsColumn;
	}

	public String getSrsColumn() {
		return srsColumn;
	}

	public String getLocationColumn() {
		return locationColumn;
	}

	public void setLocationColumn(String locationColumn) {
		this.locationColumn = locationColumn;
	}

	public boolean isGeoreferenced() {
		return ((xColumn != null && yColumn != null) || locationColumn != null);
	}

	public String getxColumn() {
		return xColumn;
	}

	public void setxColumn(String xColumn) {
		this.xColumn = xColumn;
	}

	public String getyColumn() {
		return yColumn;
	}

	public void setyColumn(String yColumn) {
		this.yColumn = yColumn;
	}

	public String getClusterColumn() {
		return clusterColumn;
	}

	public void setClusterColumn(String clusterColumn) {
		this.clusterColumn = clusterColumn;
	}

	public String getUnitNoColumn() {
		return unitNoColumn;
	}

	public void setUnitNoColumn(String unitNoColumn) {
		this.unitNoColumn = unitNoColumn;
	}

	public boolean isSamplingUnit() {
		return unitNoColumn != null;
	}
	
	
}


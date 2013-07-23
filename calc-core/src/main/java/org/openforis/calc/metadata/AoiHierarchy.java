package org.openforis.calc.metadata;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.openforis.calc.common.UserObject;
import org.openforis.calc.engine.Workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a AOI Hierarchy. A hierarchy might be an "Administrative division" or "Ecological division" of an area.
 * The hierarchy then has several hierarchy levels. e.g. country, region, province, distric for the Administrative division
 *  
 * 
 * @author A. Sanchez-Paus Diaz
 */
@javax.persistence.Entity
@Table(name = "aoi_hierarchy")
public class AoiHierarchy extends UserObject {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	@JsonIgnore
	private Workspace workspace;

	@OneToMany(mappedBy = "hierarchy", cascade = CascadeType.ALL)
	@OrderBy("rank")
	private List<AoiHierarchyLevel> levels;

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public List<AoiHierarchyLevel> getLevels() {
		return levels;
	}

	public void setLevels(List<AoiHierarchyLevel> levels) {
		this.levels = levels;
	}
}
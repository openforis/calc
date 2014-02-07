package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.openforis.calc.common.NamedUserObject;
import org.openforis.calc.engine.Workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a AOI Hierarchy. A hierarchy might be an "Administrative division" or "Ecological division" of an area. The hierarchy then
 * has several hierarchy levels. e.g. country, region, province, distric for the Administrative division
 * 
 * 
 * @author A. Sanchez-Paus Diaz
 * @author Mino Togna
 */
@javax.persistence.Entity
@Table(name = "aoi_hierarchy")
public class AoiHierarchy extends NamedUserObject {

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;

	@OneToMany(mappedBy = "hierarchy", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy("rank")
	private Set<AoiLevel> levels;

	@Transient
	private Aoi rootAoi;
	
	public Workspace getWorkspace() {
		return this.workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Set<AoiLevel> getLevels() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableSet( levels );
	}

	public Collection<AoiLevel> getLevelsReverseOrder() {
		List<AoiLevel> aoiLevels = new ArrayList<AoiLevel>( this.levels );
		
		Collections.reverse(aoiLevels);
		
		return org.openforis.commons.collection.CollectionUtils.unmodifiableCollection( aoiLevels );
	}
	
	public void setLevels(Set<AoiLevel> levels) {
		this.levels = levels;
	}
	
	@JsonIgnore
	public AoiLevel getLeafLevel() {
		return (AoiLevel) CollectionUtils.get(levels, levels.size() - 1);
	}

	public Aoi getRootAoi() {
		return rootAoi;
	}

	public void setRootAoi(Aoi rootAoi) {
		this.rootAoi = rootAoi;
	}

}

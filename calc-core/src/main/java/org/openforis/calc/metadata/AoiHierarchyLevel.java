package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.BaseEntity;

/**
 * Provides metadata about a AOI Hierarchy Level. A hierarchy might be an "Administrative division" or "Ecological division" of an area.
 * The hierarchy then has several hierarchy levels. e.g. country, region, province, distric for the Administrative division
 *  
 * 
 * @author A. Sanchez-Paus Diaz
 */
@javax.persistence.Entity
@Table(name = "aoi_level")
public class AoiHierarchyLevel extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "aoi_hierarchy_id")
	private AoiHierarchy aoiHierarchyId;

	/*@OneToMany(mappedBy = "aoiLevelId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderBy("name")
	private List<AoiItem> items;
	*/

	@Column(name = "rank")
	private Integer rank;

	public AoiHierarchy getAoiHierarchyId() {
		return aoiHierarchyId;
	}

	public void setAoiHierarchyId(AoiHierarchy aoiHierarchyId) {
		this.aoiHierarchyId = aoiHierarchyId;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}
}

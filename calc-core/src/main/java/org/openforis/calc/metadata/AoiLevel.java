package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.NamedUserObject;

/**
 * Provides metadata about a AOI Hierarchy Level. A hierarchy might be an "Administrative division" or "Ecological division" of an area.
 * The hierarchy then has several hierarchy levels. e.g. country, region, province, distric for the Administrative division
 *  
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 */
@javax.persistence.Entity
@Table(name = "aoi_level")
public class AoiLevel extends NamedUserObject {

	private static final String DIMENSION_TABLE_FORMAT = "_%s_%s_aoi_dim";
	private static final String FK_COLUMN_FORMAT = "_%s_%s_id";

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "aoi_hierarchy_id")
	private AoiHierarchy hierarchy;

	@Column(name = "rank")
	private Integer rank;

	public AoiHierarchy getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(AoiHierarchy aoiHierarchyId) {
		this.hierarchy = aoiHierarchyId;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getDimensionTable() {
		return String.format(DIMENSION_TABLE_FORMAT, hierarchy.getName(), getName());
	}

	/**
	 * 
	 * @return the name of the aoi id in output fact tables
	 */
	public String getFkColumn() {
		return String.format(FK_COLUMN_FORMAT, hierarchy.getName(), getName());
	}
}

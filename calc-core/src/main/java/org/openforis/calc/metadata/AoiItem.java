package org.openforis.calc.metadata;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.openforis.calc.common.BaseEntity;

/**
 * Provides metadata about an specific AOI.
 * For instance the AOI could be Tanzania, which would be part og the "Country" AOI level, 
 * which in its turn would be part of the "Administrative division" AOI Hierarchy.
 * 
 * 
 * @author A. Sanchez-Paus Diaz
 */
//@javax.persistence.Entity
//@Table(name = "aoi")
public class AoiItem extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "aoi_level_id")
	private AoiHierarchyLevel aoiLevelId;

	//@ManyToOne
	//@JoinColumn(name = "parent")
	private AoiItem parent;

	//@OneToMany(mappedBy = "parent")
	//	private List<AoiItem> children;

	@Column(name = "code")
	private String code;

	@Column(name = "total_area")
	private BigDecimal totalArea;

	@Column(name = "land_area")
	private BigDecimal landArea;

	public AoiHierarchyLevel getAoiLevelId() {
		return aoiLevelId;
	}

	public void setAoiLevelId(AoiHierarchyLevel aoiLevelId) {
		this.aoiLevelId = aoiLevelId;
	}

	public AoiItem getParent() {
		return parent;
	}

	public void setParent(AoiItem parent) {
		this.parent = parent;
	}

	//	public List<AoiItem> getChildren() {
	//		return children;
	//	}
	//
	//	public void setChildren(List<AoiItem> children) {
	//		this.children = children;
	//	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BigDecimal getTotalArea() {
		return totalArea;
	}

	public void setTotalArea(BigDecimal totalArea) {
		this.totalArea = totalArea;
	}

	public BigDecimal getLandArea() {
		return landArea;
	}

	public void setLandArea(BigDecimal landArea) {
		this.landArea = landArea;
	}

}

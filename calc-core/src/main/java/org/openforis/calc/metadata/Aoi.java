package org.openforis.calc.metadata;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.openforis.calc.common.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about an AOI.
 * 
 * @author Mino Togna
 * 
 */
@javax.persistence.Entity
@Table(name = "aoi")
public class Aoi extends Identifiable {

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "aoi_level_id")
	private AoiLevel aoiLevel;

	@Column(name = "code")
	private String code;

	@Column(name = "land_area")
	private Double landArea;

	@Column(name = "caption")
	private String caption;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_aoi_id")
	private Aoi parentAoi;

	@OneToMany(mappedBy = "parentAoi", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy("id")
	private Set<Aoi> children;

	public Aoi(){
	}
	
	public Aoi(Integer id, String code, String caption, Double landArea) {
		super();
		setId(id);
		setCode(code);
		setCaption(caption);
		setLandArea(landArea);
	}

	public AoiLevel getAoiLevel() {
		return aoiLevel;
	}

	public void setAoiLevel(AoiLevel aoiLevel) {
		this.aoiLevel = aoiLevel;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Double getLandArea() {
		return landArea;
	}

	public void setLandArea(Double landArea) {
		this.landArea = landArea;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Aoi getParentAoi() {
		return parentAoi;
	}

	public void setParentAoi(Aoi parentAoi) {
		this.parentAoi = parentAoi;
	}

	public Set<Aoi> getChildren() {
		return children;
	}
	
	void setChildren(Collection<Aoi> children) {
		this.children = new LinkedHashSet<Aoi>(children);
	}
	
}

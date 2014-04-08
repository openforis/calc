package org.openforis.calc.metadata;

import java.util.HashSet;
import java.util.Set;

import org.openforis.calc.persistence.jooq.tables.pojos.AoiLevelBase;
import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a AOI Hierarchy Level. A hierarchy might be an "Administrative division" or "Ecological division" of an area. The hierarchy then has several hierarchy
 * levels. e.g. country, region, province, distric for the Administrative division
 * 
 * @author Mino Togna
 * @author S. Ricci
 */
public class AoiLevel extends AoiLevelBase {

	private static final long serialVersionUID = 1L;
	private static final String DIMENSION_TABLE_FORMAT = "_%s_%s_aoi_dim";
	private static final String FK_COLUMN_FORMAT = "_%s_%s_id";
	private static final String CAPTION_COLUMN_FORMAT = "_%s_%s_caption";
	private static final String CODE_COLUMN_FORMAT = "_%s_%s_code";
	private static final String AREA_COLUMN_FORMAT = "_%s_%s_area";
	
	@JsonIgnore
	private AoiHierarchy hierarchy;
	@JsonIgnore
	private Set<Aoi> aois;

	public AoiHierarchy getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(AoiHierarchy aoiHierarchyId) {
		this.hierarchy = aoiHierarchyId;
	}

	public String getDimensionTable() {
		return String.format(DIMENSION_TABLE_FORMAT, hierarchy.getName(), getName());
	}

	/**
	 * 
	 * @return the name of the aoi id in output fact tables
	 */
	public String getFkColumn() {
		return normalize( String.format(FK_COLUMN_FORMAT, hierarchy.getName(), getName()) ) ;
	}

	public String getCaptionColumn() {
		return normalize( String.format(CAPTION_COLUMN_FORMAT, hierarchy.getName(), getName()) ) ;
	}
	
	public String getCodeColumn() {
		return normalize( String.format(CODE_COLUMN_FORMAT, hierarchy.getName(), getName()) ) ;
	}
	
	public String getAreaColumn() {
		return normalize( String.format(AREA_COLUMN_FORMAT, hierarchy.getName(), getName()) ) ;
	}
	
	private String normalize(String string){
		return string.replaceAll("\\W", "_").toLowerCase();
	}
	
	public Set<Aoi> getAois() {
		return CollectionUtils.unmodifiableSet(aois);
	}

	public void setAois(Set<Aoi> aois) {
		this.aois = aois;
	}

	public void addAoi(Aoi aoi) {
		if (this.aois == null) {
			this.aois = new HashSet<Aoi>();
		}
		this.aois.add(aoi);
	}

	public String getNormalizedName() {
		return normalize( getName() ) ;
	}

}

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

	public void setHierarchy(AoiHierarchy aoiHierarchy) {
		this.hierarchy = aoiHierarchy;
		setAoiHierarchyId( aoiHierarchy.getId() );
	}

	@JsonIgnore
	public String getDimensionTable() {
		return String.format(DIMENSION_TABLE_FORMAT, hierarchy.getName(), getName());
	}

	/**
	 * 
	 * @return the name of the aoi id in output fact tables
	 */
	@JsonIgnore
	public String getFkColumn() {
		return normalize( String.format(FK_COLUMN_FORMAT, hierarchy.getName(), getName()) ) ;
	}
	
	@JsonIgnore
	public String getCaptionColumn() {
		return normalize( String.format(CAPTION_COLUMN_FORMAT, hierarchy.getName(), getName()) ) ;
	}
	@JsonIgnore
	public String getCodeColumn() {
		return normalize( String.format(CODE_COLUMN_FORMAT, hierarchy.getName(), getName()) ) ;
	}
	@JsonIgnore
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
	@JsonIgnore
	public String getNormalizedName() {
		return normalize( getName() ) ;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AoiLevel other = (AoiLevel) obj;
		
		Integer id = getId();
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if ( !id.equals(other.getId()) )
			return false;
		return true;
	}

	
	
}

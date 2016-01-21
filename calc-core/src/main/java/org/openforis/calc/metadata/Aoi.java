package org.openforis.calc.metadata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openforis.calc.persistence.jooq.tables.pojos.AoiBase;
import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides metadata about an AOI.
 * 
 * @author Mino Togna
 * 
 */
public class Aoi extends AoiBase {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private AoiLevel aoiLevel;
	
	@JsonIgnore
	private Aoi parentAoi;
	
	@JsonProperty
	private Set<Aoi> children;

	public Aoi() {
	}
	
	public Aoi(Integer id, String code, String caption, double landArea) {
		super();
		setId(id);
		setCode(code);
		setCaption(caption);
		setLandArea( new BigDecimal(landArea) );
	}

	public AoiLevel getAoiLevel() {
		return aoiLevel;
	}

	public void setAoiLevel(AoiLevel aoiLevel) {
		this.aoiLevel = aoiLevel;
		setAoiLevelId( aoiLevel.getId() );
	}

	public Aoi getParentAoi() {
		return parentAoi;
	}

	public void setParentAoi(Aoi parentAoi) {
		this.parentAoi = parentAoi;
		
		Integer parentId = (parentAoi == null) ? null : parentAoi.getId();
		setParentAoiId( parentId );
	}

	public Set<Aoi> getChildren() {
		return CollectionUtils.unmodifiableSet( children );
	}
	
	void setChildren(Collection<Aoi> children) {
		this.children = new LinkedHashSet<Aoi>(children);
		for (Aoi child : children) {
			child.setParentAoi( this );
		}
	}
	
	void addChild( Aoi aoi ){
		if( this.children == null ){
			this.children = new LinkedHashSet<Aoi>();
		}
		this.children.add( aoi );
		aoi.setParentAoi( this );
	}
}

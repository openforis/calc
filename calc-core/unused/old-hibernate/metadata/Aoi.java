package org.openforis.calc.metadata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openforis.calc.persistence.jooq.tables.pojos.AoiBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.AoiHierarchyBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a AOI Hierarchy. A hierarchy might be an "Administrative division" or "Ecological division" of an area. The hierarchy then
 * has several hierarchy levels. e.g. country, region, province, distric for the Administrative division
 * 
 * @author Mino Togna
 * @author S. Ricci 
 */
public class AoiHierarchy extends AoiHierarchyBase {
	
	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Workspace workspace;

//	@OneToMany(mappedBy = "hierarchy", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//	@OrderBy("rank")
	private List<AoiLevel> levels;

	private Aoi rootAoi;
	
	public Workspace getWorkspace() {
		return this.workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public List<AoiLevel> getLevels() {
		return org.openforis.commons.collection.CollectionUtils.unmodifiableList( levels );
	}

	public Collection<AoiLevel> getLevelsReverseOrder() {
		List<AoiLevel> aoiLevels = new ArrayList<AoiLevel>( this.levels );
		
		Collections.reverse(aoiLevels);
		
		return org.openforis.commons.collection.CollectionUtils.unmodifiableCollection( aoiLevels );
	}
	
//	public void addLevel(AoiLevel level) {
//		if( this.levels == null ){
//			this.levels = new ArrayList<AoiLevel>();
//		}
//		level.setHierarchy( this );
//		this.levels.add( level );
//	}
	
	public void setLevels(List<AoiLevel> levels) {
		Collections.sort( levels , new Comparator<AoiLevel>() {
			@Override
			public int compare(AoiLevel o1, AoiLevel o2) {
				return o1.getRank().compareTo( o2.getRank() );
			}
		} );
		this.levels = levels;
		for (AoiLevel aoiLevel : this.levels) {
			aoiLevel.setHierarchy( this );
		}
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

	public Aoi getAoiById( int aoiId ) {
		for( AoiLevel aoiLevel : this.getLevels() ) {
			Set<Aoi> aois = aoiLevel.getAois();
			for( Aoi aoi : aois ) {
				if( aoi.getId().equals(aoiId) ) {
					return aoi;
				}
			}
		}
		return null;
	}

	public AoiLevel getLevelById(Integer aoiLevelId) {
		for (AoiLevel aoiLevel : this.levels) {
			if( aoiLevel.getId().equals(aoiLevel.getId()) ){
				return aoiLevel;
			}
		}
		return null;
	}

	public void clearLevels() {
		this.levels = null;
	}

}

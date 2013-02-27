package org.openforis.calc.model;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class AoiHierarchyMetadata extends AoiHierarchy {

	private static final long serialVersionUID = 1L;
	
	private AoiHierarchy aoiHierarchy;
	private List<AoiHierarchyLevelMetadata> levels;

	public AoiHierarchyMetadata(AoiHierarchy aoiHierarchy,
			List<AoiHierarchyLevelMetadata> levels) {
		this.aoiHierarchy = aoiHierarchy;
		this.levels = levels;
		for (AoiHierarchyLevelMetadata level : levels) {
			level.setAoiHierachyMetadata(this);
		}
	}

	public Integer getId() {
		return aoiHierarchy.getId();
	}

	public Integer getAoiHierarchyId() {
		return aoiHierarchy.getAoiHierarchyId();
	}

	public Integer getSurveyId() {
		return aoiHierarchy.getSurveyId();
	}

	public String getAoiHierarchyName() {
		return aoiHierarchy.getAoiHierarchyName();
	}

	public String getAoiHierarchyLabel() {
		return aoiHierarchy.getAoiHierarchyLabel();
	}

	public String getAoiHierarchyDescription() {
		return aoiHierarchy.getAoiHierarchyDescription();
	}

	public List<AoiHierarchyLevelMetadata> getLevelMetadata() {
		return Collections.unmodifiableList(levels);
	}
	
	public int getMaxRank() {
		return levels.get(levels.size()-1).getAoiHierarchyLevelRank();
	}
}

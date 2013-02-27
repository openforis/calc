package org.openforis.calc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class AoiHierarchyLevelMetadata extends AoiHierarchyLevel{

	private static final long serialVersionUID = 1L;

	private AoiHierarchyLevel level;
	private AoiHierarchyMetadata aoiHierachyMetadata;

	public AoiHierarchyLevelMetadata(AoiHierarchyLevel level) {
		this.level = level;
	}

	void setAoiHierachyMetadata(AoiHierarchyMetadata aoiHierachyMetadata) {
		this.aoiHierachyMetadata = aoiHierachyMetadata;
	}
	
	public Integer getId() {
		return level.getId();
	}

	public Integer getAoiHierarchyLevelId() {
		return level.getAoiHierarchyLevelId();
	}

	public Integer getAoiHierarchyId() {
		return level.getAoiHierarchyId();
	}

	public String getAoiHierarchyLevelName() {
		return level.getAoiHierarchyLevelName();
	}

	public String getAoiHierarchyLevelLabel() {
		return level.getAoiHierarchyLevelLabel();
	}

	public Integer getAoiHierarchyLevelRank() {
		return level.getAoiHierarchyLevelRank();
	}

	public AoiHierarchyMetadata getAoiHierachyMetadata() {
		return aoiHierachyMetadata;
	}
	
	public static List<AoiHierarchyLevelMetadata> fromList(List<AoiHierarchyLevel> levels) {
		List<AoiHierarchyLevelMetadata> levelMetadata = new ArrayList<AoiHierarchyLevelMetadata>();
		for (AoiHierarchyLevel level : levels) {
			AoiHierarchyLevelMetadata lm = new AoiHierarchyLevelMetadata(level);
			levelMetadata.add(lm);
		}
		return levelMetadata;
	}
}

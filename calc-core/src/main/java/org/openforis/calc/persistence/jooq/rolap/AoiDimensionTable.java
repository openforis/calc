package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.AoiHierarchyLevelMetadata;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class AoiDimensionTable extends HierarchicalDimensionTable {

	private static final long serialVersionUID = 1L;

	private AoiHierarchyLevelMetadata aoiHierarchyLevelMetadata;
	
	AoiDimensionTable(String schema, AoiHierarchyLevelMetadata level, AoiDimensionTable parentTable) {
		super(schema, level.getAoiHierarchyLevelName(), parentTable);
		this.aoiHierarchyLevelMetadata = level;
	}
	
	public AoiHierarchyLevelMetadata getAoiHierarchyLevelMetadata() {
		return aoiHierarchyLevelMetadata;
	}
}
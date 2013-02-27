package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.AoiHierarchyLevelMetadata;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class AoiDimensionTable extends HierarchicalDimensionTable<AoiDimensionRecord> {

	private static final long serialVersionUID = 1L;

	private AoiHierarchyLevelMetadata aoiHierarchyLevelMetadata;
	
	AoiDimensionTable(String schema, AoiHierarchyLevelMetadata level) {
		super(schema, level.getAoiHierarchyLevelName(), AoiDimensionRecord.class);
		this.aoiHierarchyLevelMetadata = level;
	}
	
	public AoiHierarchyLevelMetadata getAoiHierarchyLevelMetadata() {
		return aoiHierarchyLevelMetadata;
	}
}
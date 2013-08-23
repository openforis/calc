package org.openforis.calc.rdb;

import org.openforis.calc.metadata.AoiHierarchyLevel;

/**
 * 
 * @author M. Togna
 * 
 */
public class AoiDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;
	private AoiHierarchyLevel hierarchyLevel;

	AoiDimensionTable(RelationalSchema schema, AoiHierarchyLevel aoiHierarchyLevel) {
		super(aoiHierarchyLevel.getDimensionTable(), schema);
		this.hierarchyLevel = aoiHierarchyLevel;
	}

	public AoiHierarchyLevel getHierarchyLevel() {
		return hierarchyLevel;
	}
}

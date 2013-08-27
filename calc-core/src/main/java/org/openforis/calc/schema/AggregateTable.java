package org.openforis.calc.schema;

import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class AggregateTable extends FactTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_%s_stratum_agg";
	private AoiHierarchyLevel aoiHierarchyLevel;

	AggregateTable(FactTable factTable, AoiHierarchyLevel level) {
		super(factTable.getEntity(), getName(factTable, level), factTable.getSchema(), factTable, null);
		this.aoiHierarchyLevel = level;
		Entity entity = factTable.getEntity();
		createDimensionFields(entity);
		createStratumIdField();
		createAoiIdFields(level);
		createMeasureFields(entity);
	}

	private static String getName(FactTable factTable, AoiHierarchyLevel level) {
		String entityName = factTable.getEntity().getName();
		String levelName = level.getName();
		return String.format(TABLE_NAME_FORMAT, entityName, levelName);
	}
	
	public AoiHierarchyLevel getAoiHierarchyLevel() {
		return aoiHierarchyLevel;
	}
}

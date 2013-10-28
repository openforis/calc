/**
 * 
 */
package org.openforis.calc.schema;

import org.openforis.calc.schema.Hierarchy.Level;
import org.openforis.calc.schema.Hierarchy.Table;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class StratumDimension extends Dimension {

	private static final String STRATUM = "Stratum";
	private StratumDimensionTable table;

	StratumDimension(RolapSchema rolapSchema, StratumDimensionTable stratumDimensionTable) {
		super(rolapSchema);
		this.table = stratumDimensionTable;

		setName(STRATUM);
		setCaption(STRATUM);
		createHierarchy();
	}

	private void createHierarchy() {
		Hierarchy hierarchy = new Hierarchy(STRATUM);

		Table t = new Table(table.getSchema().getName(), table.getName());
		hierarchy.setTable(t);

		Level level = new Level(STRATUM, table.ID.getName(), table.CAPTION.getName());
		hierarchy.addLevel(level);

		setHierarchy(hierarchy);
	}

}

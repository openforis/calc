/**
 * 
 */
package org.openforis.calc.schema;

import java.util.Collection;

import org.jooq.Record;
import org.jooq.SelectQuery;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.schema.Hierarchy.Level;
import org.openforis.calc.schema.Hierarchy.View;

/**
 * @author M. Togna
 * 
 */
public class AoiDimension extends Dimension {

	private AoiHierarchy aoiHierarchy;
	private AoiHierarchyFlatTable aoiHierarchyFlatTable;

	AoiDimension(RolapSchema rolapSchema, AoiHierarchyFlatTable aoiHierarchyFlatTable) {
		super(rolapSchema);
		this.aoiHierarchyFlatTable = aoiHierarchyFlatTable;
		this.aoiHierarchy = this.aoiHierarchyFlatTable.getAoiHierarchy();

		setName(aoiHierarchy.getName());

		createHierarchy();
	}

	private void createHierarchy() {
		String aoiHierarchyName = aoiHierarchy.getName();
		Hierarchy hierarchy = new Hierarchy(aoiHierarchyName);

		Collection<AoiLevel> levels = aoiHierarchy.getLevelsReverseOrder();
		for (AoiLevel aoiLevel : levels) {

			String aoiLevelName = aoiLevel.getName();
			String aliasIdColumn = aoiHierarchyFlatTable.getAoiIdField(aoiLevel).getName();
			String aliasCaptionColumn = aoiHierarchyFlatTable.getAoiCaptionField(aoiLevel).getName();

			Level level = new Level( aoiLevelName, aliasIdColumn, aliasCaptionColumn );
			hierarchy.addLevel(0, level);
		}

		SelectQuery<Record> select = aoiHierarchyFlatTable.getSelectQuery();
		View view = new View(aoiHierarchyName, select.toString());
		hierarchy.setView(view);

		setHierarchy(hierarchy);
	}

	AoiHierarchy getAoiHierarchy() {
		return aoiHierarchy;
	}

}

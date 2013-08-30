/**
 * 
 */
package org.openforis.calc.schema;

import java.util.List;

import org.jooq.Record;
import org.jooq.SelectQuery;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.Hierarchy.Level;
import org.openforis.calc.schema.Hierarchy.View;

/**
 * @author M. Togna
 * 
 */
public class AoiDimension extends Dimension {

	private AoiHierarchy aoiHierarchy;

	AoiDimension(RolapSchema rolapSchema, AoiHierarchy aoiHierarchy) {
		super(rolapSchema);
		this.aoiHierarchy = aoiHierarchy;

		setName(aoiHierarchy.getName());

		createHierarchy();
	}

	private void createHierarchy() {
		OutputSchema outputSchema = getRolapSchema().getOutputSchema();
		String aoiHierarchyName = aoiHierarchy.getName();

		Hierarchy hierarchy = new Hierarchy(aoiHierarchyName);

		SelectQuery<Record> select = new Psql().selectQuery();
		List<AoiLevel> aoiLevels = aoiHierarchy.getLevels();
		for ( int i = aoiLevels.size() - 1 ; i >= 0 ; i-- ) {
			AoiLevel aoiLevel = aoiLevels.get(i);
			AoiDimensionTable aoiDimTable = outputSchema.getAoiDimensionTable(aoiLevel);

			String aoiLevelName = aoiLevel.getName();

			String aliasIdColumn = aoiLevelName + "_id";
			String aliasCaptionColumn = aoiLevelName + "_caption";

			select.addSelect(aoiDimTable.ID.as(aliasIdColumn));
			select.addSelect(aoiDimTable.CAPTION.as(aliasCaptionColumn));

			if ( i == aoiLevels.size() - 1 ) {
				select.addFrom(aoiDimTable);
			} else {
				AoiLevel childLevel = aoiLevels.get(i + 1);
				AoiDimensionTable childAoiDimTable = outputSchema.getAoiDimensionTable(childLevel);

				select.addJoin(aoiDimTable, childAoiDimTable.PARENT_AOI_ID.eq(aoiDimTable.ID));
			}

			Level level = new Level(aoiLevelName, aliasIdColumn, aliasCaptionColumn);
			hierarchy.addLevel(0, level);
		}

		View view = new View(aoiHierarchyName, select.getSQL());
		hierarchy.setView(view);

		setHierarchy(hierarchy);
	}
	
	AoiHierarchy getAoiHierarchy() {
		return aoiHierarchy;
	}

}

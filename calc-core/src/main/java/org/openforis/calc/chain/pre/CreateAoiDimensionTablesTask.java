package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 */
public final class CreateAoiDimensionTablesTask extends Task {

	private static final String CALC_AOI_TABLE = "calc.aoi";
	private static final String DIMENSION_ID_COLUMN = "id";
	private static final String AOI_LEVEL_ID_COLUMN = "aoi_level_id";

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy hierarchy : hierarchies) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			for (AoiHierarchyLevel level : levels) {
				String tableName = Psql.quote(level.getDimensionTable());
				Integer varId = level.getId();

				Psql select = new Psql()
					.select("*")
					.from(CALC_AOI_TABLE)
					.where(AOI_LEVEL_ID_COLUMN+"=?");
				
				psql()
					.createTable(tableName)
					.as(select) 
					.execute(varId);
				
				psql()
					.alterTable(tableName)
					.addPrimaryKey(DIMENSION_ID_COLUMN);
			}
		}
	}

}
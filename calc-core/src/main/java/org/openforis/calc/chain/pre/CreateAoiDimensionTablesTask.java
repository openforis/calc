package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
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
public final class CreateAoiDimensionTablesTask extends SqlTask {

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy hierarchy : hierarchies) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			for (AoiHierarchyLevel level : levels) {
				String tableName = level.getDimensionTable();

				Integer varId = level.getId();

				Psql select = new Psql()
					.select("*")
					.from("calc.aoi")
					.where("aoi_level_id = ?");
				
				psql()
					.createTable(tableName)
					.as(select) 
					.execute(varId);
				
				psql()
					.alterTable(tableName)
					.addPrimaryKey("id");
			}
		}
	}

}
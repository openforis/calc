package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import static org.openforis.calc.persistence.postgis.Psql.*;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Task responsible for assigning AOI codes and/or ids to an output table based on a Point column.
 * Assigns AOI ids to each georeferenced entity
 * 
 * @author G. Miceli
 */
public final class CreateAoiColumnsTask extends SqlTask {
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			if (entity.isGeoreferenced()) {
				createAoiColumns(entity);
			}
		}
	}

	private void createAoiColumns(Entity entity) {
		Workspace workspace = entity.getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy hierarchy : hierarchies) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			for (AoiHierarchyLevel level : levels) {
				createAoiColumns(entity, level);
			}
		}
	}

	private void createAoiColumns(Entity entity, AoiHierarchyLevel level) {
		String dataTable = quote(entity.getDataTable());
		String factIdColumn = quote(entity.getIdColumn());
		String aoiFkColumn = quote(level.getFkColumn());
		String aoiDimTable = quote(level.getDimensionTable());
		
		// add AOI id column to fact table output schema
		psql()
			.alterTable(dataTable)
			.addColumn(aoiFkColumn, INTEGER)
			.execute();
		
		// update values
		Psql selectAois = new Psql()
			.select("f."+factIdColumn+" as fid", "a.id as aid")
			.from(dataTable+" f")
			.innerJoin(aoiDimTable+" a").on("ST_Contains(a.shape, f."+CreateLocationColumnsTask.LOCATION_COLUMN+")");
			
		psql()
			.with("tmp", selectAois)
			.update(dataTable+" f")
				.set(aoiFkColumn, "aid")
				.from("tmp")
				.where("f."+factIdColumn+" = tmp.fid")
				.execute();
	}
}



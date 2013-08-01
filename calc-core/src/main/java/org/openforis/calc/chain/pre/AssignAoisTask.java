package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.postgis.Psql;
/**
 * Task responsible for assigning AOI codes and/or ids to an output table based on a Point column.
 * Assigns AOI ids to each georeferenced entity
 * 
 * @author G. Miceli
 */
public final class AssignAoisTask extends SqlTask {
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			if (entity.isGeoreferenced()) {
				assignAois(entity);
			}
		}
	}

	private void assignAois(Entity entity) {
		Workspace workspace = entity.getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy hierarchy : hierarchies) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			for (AoiHierarchyLevel level : levels) {
				assignAois(entity, level);
			}
		}
	}

	private void assignAois(Entity entity, AoiHierarchyLevel level) {
		AoiHierarchy hierarchy = level.getHierarchy();
		String dataTable = entity.getDataTable();
		String aoiIdColumn = "_"+hierarchy.getName()+"_"+level.getName()+"_id";
		String aoiDimTable = level.getDimensionTable();
		
		// add AOI id column to fact table output schema
		psql()
			.alterTable(dataTable)
			.addColumn(aoiIdColumn, Psql.INTEGER)
			.execute();
		
		// update values
		Psql selectAois = new Psql()
			.select("f.id as fid", "a.id as aid")
			.from(dataTable+" f")
			.innerJoin(aoiDimTable, "ST_Contains(a.shape, f._location))");
			
		psql()
			.with("tmp", selectAois)
			.update(dataTable)
			.set(aoiIdColumn+" = a.id")
			.from("tmp")
			.where("id = tmp.fid")
			.execute();
		
//		executeSql(
//				"WITH tmp AS ("+
//				"SELECT f.id as fid, a.id as aid "+
//						"FROM %s f "+
//						"INNER JOIN %s a ON ST_Contains(a.shape, f._location))"+
//				"UPDATE %s SET %s = a.id FROM tmp WHERE id = tmp.fid",
//				dataTable, aoiDimTable, dataTable, aoiIdColumn
//		);
	}

}



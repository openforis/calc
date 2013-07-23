package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.sql.Sql.quoteIdentifier;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
/**
 * Task responsible for assigning AOI codes and/or ids to an output table based on a Point column.
 * 
 * @author G. Miceli
 */
public final class AssignAoisTask extends Task {
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			if (entity.isGeoreferenced()) {
				List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
				for (AoiHierarchy hierarchy : hierarchies) {
					List<AoiHierarchyLevel> levels = hierarchy.getLevels();
					for (AoiHierarchyLevel level : levels) {

						setSchema(ws.getOutputSchema());
						String dataTable = quoteIdentifier(entity.getDataTable());
						String aoiIdColumn = quoteIdentifier("_"+hierarchy.getName()+"_"+level.getName()+"_id");
						String aoiDimTable = quoteIdentifier(level.getDimensionTable());
						
						// add AOI id column to fact table output schema
						executeSql("ALTER TABLE %s ADD COLUMN %s INTEGER", 
								   dataTable, aoiIdColumn);
						executeSql(
								"WITH tmp AS ("+
								"SELECT f.id as fid, a.id as aid "+
										"FROM %s f "+
										"INNER JOIN %s a ON ST_Contains(a.shape, f._location))"+
								"UPDATE %s SET %s = a.id FROM tmp WHERE id = tmp.fid",
								dataTable,aoiDimTable, dataTable
								// TODO

								);
						
						// TODO updates values, find using ST_Contains(aoi area, location)
					}
				}
			}
		}
	}
}

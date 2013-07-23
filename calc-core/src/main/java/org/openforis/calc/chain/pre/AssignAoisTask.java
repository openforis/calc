package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Task responsible for assigning AOI codes and/or ids to an output table based on a Point column.
 * 
 * @author G. Miceli
 */
public final class AssignAoisTask extends Task {
	@Override
	protected void execute() throws Throwable {
//		Workspace ws = getWorkspace();
//		List<Entity> entities = ws.getEntities();
//		for (Entity entity : entities) {
//			if ( entity.isGeoreferenced() )
//			{
//				List<AoiHierarchy> hierarchies = entity.getAoiHierarchies();
//				for (AoiHierarchy hierarchy : hierarchies) {
//					List<AoiLevel> levels = hierarchy.getLevels();
//					for (AoiLevel level : levels) {
//						String outputSchema = ws.getOutputSchema();
//						String dataTable = entity.getDataTable();
//						// add AOI id column to fact table output schema
//						executeSql("ALTER TABLE %s.%s ADD COLUMN _%s_%s_id INTEGER",
//								outputSchema,
//								dataTable,
//								hierarchy.getName(),
//								level.getName());
//						// TODO updates values, find using ST_Contains(aoi area, location)
//					}
//				}	
//			}
//		}
	}
}

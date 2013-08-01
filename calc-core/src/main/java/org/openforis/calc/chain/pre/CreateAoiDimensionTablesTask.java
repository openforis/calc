package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.sql.Psql.quoteIdentifiers;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 */
public final class CreateAoiDimensionTablesTask extends Task {

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy hierarchy : hierarchies) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			for (AoiHierarchyLevel level : levels) {
				String outputSchema = quoteIdentifiers(workspace.getOutputSchema());

				String tableName = quoteIdentifiers(level.getDimensionTable());

				Integer varId = level.getId();

				executeSql("CREATE TABLE %s.%s AS SELECT * FROM calc.aoi WHERE aoi_level_id = %d", outputSchema, tableName, varId);

				executeSql("ALTER TABLE %s.%s ADD PRIMARY KEY (id)", outputSchema, tableName);

				log().info("AOI dimension table created:" + tableName);
			}
		}
	}

}
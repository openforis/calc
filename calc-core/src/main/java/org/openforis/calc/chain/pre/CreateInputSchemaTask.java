package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.sql.Sql.quoteIdentifier;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CreateInputSchemaTask extends Task {
	Logger logger = LoggerFactory.getLogger(PrepareOutputSchemaTask.class);

	private void createOutputSchema() {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		String sql = "CREATE SCHEMA %s";
		executeSql(sql, outputSchema);
		logger.info("Output scehma %s created", outputSchema);

	}

	private void createDimensionTables() {
		Workspace workspace = getContext().getWorkspace();
		// Category Dimension
		createCategoricalDimensionTables(workspace);

		// AOI Dimension
		// Create one table for every level of the AOI hierarchy
		// Regular dimension fields + shape land area and total area
		createAoiDimensionTables(workspace);

	}

	private void createAoiDimensionTables(Workspace workspace) {
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy hierarchy : hierarchies) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			for (AoiHierarchyLevel level : levels) {
				String outputSchema = quoteIdentifier(workspace.getOutputSchema());

				String tableName = quoteIdentifier(level.getDimensionTable());

				Integer varId = level.getId();

				executeSql("CREATE TABLE %s.%s AS SELECT * FROM calc.aoi WHERE aoi_level_id = %d", outputSchema, tableName, varId);

				executeSql("ALTER TABLE %s.%s ADD PRIMARY KEY (id)", outputSchema, tableName);

				logger.info("AOI dimension table %s created.", tableName);
			}
		}
	}

	private void createCategoricalDimensionTables(Workspace workspace) {
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();

			for (Variable var : variables) {
				if (var instanceof CategoricalVariable || var instanceof BinaryVariable) {
					String outputSchema = quoteIdentifier(workspace.getOutputSchema());
					String tableName = quoteIdentifier(var.getDimensionTable());
					Integer varId = var.getId();

					executeSql("CREATE TABLE %s.%s AS SELECT * FROM calc.category WHERE variable_id = %d", outputSchema,
							tableName, varId);

					executeSql("ALTER TABLE %s.%s ADD PRIMARY KEY (id)", outputSchema, tableName);

					logger.info("Categorical dimension table %s created.", tableName);

				}
			}
		}
	}



	@Override
	protected void execute() throws Throwable {
		createOutputSchema();
		createDimensionTables();
	}

}
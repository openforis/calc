package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.sql.Psql.quoteIdentifier;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 */
public final class CreateCategoryDimensionTablesTask extends Task {

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
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

					log().info("Categorical dimension table created: " + tableName);

				}
			}
		}
	}

}
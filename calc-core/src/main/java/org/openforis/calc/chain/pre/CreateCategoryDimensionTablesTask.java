package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 */
public final class CreateCategoryDimensionTablesTask extends SqlTask {

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();

			for (Variable var : variables) {
				if (var instanceof CategoricalVariable || var instanceof BinaryVariable) {
					String tableName = var.getDimensionTable();
					Integer varId = var.getId();
					
					Psql select = new Psql()
						.select("*")
						.from("calc.category")
						.where("variable_id = ?");
					
					psql()
						.createTable(tableName)
						.as(select) 
						.execute(varId);
//					
//					executeSql("CREATE TABLE %s.%s AS SELECT * FROM calc.category WHERE variable_id = %d", outputSchema,
//							tableName, varId);
//
//					executeSql("ALTER TABLE %s.%s ADD PRIMARY KEY (id)", outputSchema, tableName);

//					log().debug("Categorical dimension table created: " + tableName);
				}
			}
		}
	}

}
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

	private static final String CALC_CATEGORY_TABLE = "calc.category";
	private static final String DIMENSION_TABLE_ID_COLUMN = "id";
	private static final String VARIABLE_ID_COLUMN = "variable_id";

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();

			for (Variable var : variables) {
				if (var instanceof CategoricalVariable && !var.isDegenerateDimension() ) {
					String tableName = Psql.quote(var.getDimensionTable());
					Integer varId = var.getId();
					
					Psql select = new Psql()
						.select("*")
						.from(CALC_CATEGORY_TABLE)
						.where(VARIABLE_ID_COLUMN+"=?");
					
					psql()
						.createTable(tableName)
						.as(select) 
						.execute(varId);
					
					psql()
					.alterTable(tableName)
					.addPrimaryKey(DIMENSION_TABLE_ID_COLUMN)
					.execute();
					
				}
			}
		}
	}

}
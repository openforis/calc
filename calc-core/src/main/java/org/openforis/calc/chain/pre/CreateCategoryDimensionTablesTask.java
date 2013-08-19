package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.postgis.Psql;
import org.springframework.transaction.annotation.Transactional;

/**
 * Copies category tables into the output schema.  Fails if output schema already exists.
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 */
public final class CreateCategoryDimensionTablesTask extends Task {

	private static final String CALC_CATEGORY_TABLE = "calc.category";
	private static final String DIMENSION_TABLE_ID_COLUMN = "id";
	private static final String VARIABLE_ID_COLUMN = "variable_id";
	private static final String NAME_COLUMN = "name";
	private static final String CAPTION_COLUMN = "caption";

	@Override
	@Transactional
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();

			for (Variable var : variables) {
				if (var instanceof CategoricalVariable && !var.isDegenerateDimension() ) {
					String dimensionTableName = Psql.quote(var.getDimensionTable());
					Integer varId = var.getId();
					
					createDimensionTable(dimensionTableName, varId);
					
					addPrimaryKeyToTable(dimensionTableName);
					
					renameColumnFromNameToCaption(dimensionTableName);
					
				}
			}
		}
	}

	private void addPrimaryKeyToTable(String dimensionTableName) {
		psql()
		.alterTable(dimensionTableName)
		.addPrimaryKey(DIMENSION_TABLE_ID_COLUMN)
		.execute();
	}

	private void createDimensionTable(String dimensionName, Integer varId) {
		Psql select = new Psql()
			.select("*")
			.from(CALC_CATEGORY_TABLE)
			.where(VARIABLE_ID_COLUMN+"=?");
		
		psql()
			.createTable(dimensionName)
			.as(select) 
			.execute(varId);
	}
	
	private void renameColumnFromNameToCaption(String dimensionTable ){
		
		psql()
			.alterTable(dimensionTable)
			.renameColumnTo(NAME_COLUMN, CAPTION_COLUMN)
			.execute();

		
	}

}
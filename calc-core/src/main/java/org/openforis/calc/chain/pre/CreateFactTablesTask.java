package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Creates fact tables in output schema based on {@link Category}s
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz
 */
public final class CreateFactTablesTask extends SqlTask {

	private static final String ID_COLUMN_SUFFIX = "_id";

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<Entity> entities = workspace.getEntities();
		String inputSchema = workspace.getInputSchema();

		for (Entity entity : entities) {
			String inputTable = inputSchema + "." + Psql.quote(entity.getDataTable());
			String outputTable = Psql.quote(entity.getDataTable());
			String idColumn = Psql.quote(entity.getIdColumn());

			createFactTable(inputTable, outputTable, idColumn);

			List<Variable> variables = entity.getVariables();
			for (Variable variable : variables) {
				// Add columns for variables not found in input schema
				if (!variable.isInput()) {
					String valueColumn = Psql.quote(variable.getValueColumn());
					if ( variable instanceof CategoricalVariable ) {
						String valueIdColumn = Psql.quote(variable.getValueColumn()+ID_COLUMN_SUFFIX);
						addCategoryValueColumn(outputTable, valueColumn);
						addCategoryIdColumn(outputTable, valueIdColumn);
					} else {
						addQuantityColumn(outputTable, valueColumn);						
					}
				}
			}
		}
	}

	private void createFactTable(String inputTable, String outputTable, String idColumn) {
		Psql select = new Psql()
			.select("*")
			.from(inputTable);

		psql().createTable(outputTable).as(select).execute();

		if ( idColumn != null ) {
			psql().alterTable(outputTable).addPrimaryKey(idColumn).execute();
		}
	}

	private void addQuantityColumn(String outputTable, String valueColumn) {
		psql()
			.alterTable(outputTable)
			.addColumn(valueColumn, Psql.FLOAT8)
			.execute();
	}

	private void addCategoryValueColumn(String outputTable, String valueColumn) {
		psql()
			.alterTable(outputTable)
			.addColumn(valueColumn, Psql.VARCHAR, 255)
			.execute();
	}

	private void addCategoryIdColumn(String outputTable, String valueIdColumn) {
		psql()
			.alterTable(outputTable)
			.addColumn(valueIdColumn, Psql.INTEGER)
			.execute();
	}
}
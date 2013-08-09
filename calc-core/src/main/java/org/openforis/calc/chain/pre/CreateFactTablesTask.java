package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.BinaryVariable;
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

	private static final String STRATUM_ID = "_stratum_id";
	private static final String DIMENSION_TABLE_ID_COLUMN = "id";
	private static final String DIMENSION_TABLE_ORIGINAL_ID_COLUMN = "original_id";
	private static final String ID_COLUMN_SUFFIX_TO_REPLACE = "_code_id";
	private static final String ID_COLUMN_SUFFIX = "_dim_id";

	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<Entity> entities = workspace.getEntities();
		String inputSchema = workspace.getInputSchema();

		for (Entity entity : entities) {
			String inputTable = inputSchema + "." + Psql.quote(entity.getDataTable());
			String outputFactTable = Psql.quote(entity.getDataTable());
			String idColumnFactTable = Psql.quote(entity.getIdColumn());

			createFactTable(inputTable, outputFactTable, idColumnFactTable);

			List<Variable> variables = entity.getVariables();
			for (Variable variable : variables) {
				// Add columns for variables not found in input schema
				if ( !variable.isInput() ) {
					String valueColumn = Psql.quote(variable.getValueColumn());
					if ( variable instanceof CategoricalVariable ) {
						String valueIdColumn = Psql.quote(variable.getValueColumn()+ID_COLUMN_SUFFIX);
						addCategoryValueColumn(outputFactTable, valueColumn);
						addCategoryIdColumn(outputFactTable, valueIdColumn);
					} else {
						addQuantityColumn(outputFactTable, valueColumn);						
					}

				} else if( variable instanceof CategoricalVariable  && !(variable instanceof BinaryVariable) && !variable.isDegenerateDimension() ) {

					// CHANGE THE VALUES FROM THE ORIGINAL_ID TO THE INTERNAL ID OF THE CATEGORICAL DIMENSION TABLE
					String dimensionTable = variable.getDimensionTable();
					String categoryColumn = variable.getCategoryColumn();
					
				
					updateDimensionIdColumn(outputFactTable, dimensionTable, categoryColumn);
					
					String newColumnName = renameDimensionIdColumn(outputFactTable, categoryColumn);
					
					// ADD FK relationship
					addDimensionTableFK(outputFactTable, dimensionTable, newColumnName);
				}
			}
		}
	}

	private void addDimensionTableFK(String outputFactTable,
			String dimensionTable, String categoryColumn) {
		psql()
			.alterTable(outputFactTable)
			.addForeignKey( categoryColumn, dimensionTable, DIMENSION_TABLE_ID_COLUMN)
			.execute();
	}

	private void updateDimensionIdColumn(String outputFactTable,
			String dimensionTable, String categoryColumn) {
		psql()
			.update( outputFactTable )
			.set(categoryColumn  + "= " + dimensionTable+ "."+ DIMENSION_TABLE_ID_COLUMN )
			.from( dimensionTable  )
			.where( outputFactTable+"."+categoryColumn + " = " + dimensionTable + "." + DIMENSION_TABLE_ORIGINAL_ID_COLUMN )
			.execute();
	}
	
	private String renameDimensionIdColumn(String outputFactTable, String categoryColumn ){
		String newColumnName = categoryColumn;
		if( categoryColumn.endsWith(ID_COLUMN_SUFFIX_TO_REPLACE)){
			newColumnName = categoryColumn.substring(0, categoryColumn.indexOf( ID_COLUMN_SUFFIX_TO_REPLACE) )  + ID_COLUMN_SUFFIX;		
			psql()
				.alterTable(outputFactTable)
				.renameColumnTo(categoryColumn, newColumnName)
				.execute();
		}
		return newColumnName;
		
	}
	

	private void createFactTable(String inputTable, String outputTable, String idColumn) {
		Psql select = new Psql()
		.select("*")
		.from(inputTable);

		psql().createTable(outputTable).as(select).execute();

		if ( idColumn != null ) {
			psql().alterTable(outputTable).addPrimaryKey(idColumn).execute();
		}
		
		// Add _stratum_id column
		psql().alterTable( outputTable).addColumn(STRATUM_ID, Psql.INTEGER).execute();
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
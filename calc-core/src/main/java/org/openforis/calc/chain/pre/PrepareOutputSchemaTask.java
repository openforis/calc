package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.sql.Sql.quoteIdentifier;

import java.util.ArrayList;
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
import org.springframework.jdbc.core.RowMapper;

/**
 * Copies input tables into the output schema.  Fails if output schema already exists.
 * 
 * 1. Drop output schema if exists
 * 2. Create output schema
 * 3. Copy code list tables
 * 4. Copy tables and columns from input schema to output schema, adding them if they do not exist.
 *    4a. Coordinate should be converted to Geometry(Point,4326) if source columns (x/y/srs) are specified
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class PrepareOutputSchemaTask extends Task {
	
	Logger logger = LoggerFactory.getLogger(PrepareOutputSchemaTask.class);

	private void dropOutputSchema() {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		String sql = "DROP SCHEMA IF EXISTS %s CASCADE";
		executeSql(sql, outputSchema);

	}

	private void createOutputSchema() {
		Workspace workspace = getContext().getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		String sql = "CREATE SCHEMA %s";
		executeSql(sql, outputSchema);

	}

	private void createTables() {

		createDimensionTables();
		createFactTables();

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

				String table = quoteIdentifier(level.getDimensionTable());

				Integer varId = level.getId();

				executeSql("CREATE TABLE %s.%s AS SELECT * FROM calc.aoi WHERE aoi_level_id = %d", 
						   outputSchema, table, varId);

				executeSql("ALTER TABLE %s.%s ADD PRIMARY KEY (id)", outputSchema, table);
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

					executeSql("CREATE TABLE %s.%s AS SELECT * FROM calc.category WHERE variable_id = %d", 
							outputSchema, tableName, varId);

					executeSql("ALTER TABLE %s.%s ADD PRIMARY KEY (id)", 
							outputSchema, tableName);
				}
			}
		}
	}

	private void createFactTables() {
		Workspace workspace = getContext().getWorkspace();
		List<Entity> entities = workspace.getEntities();
		String outputSchema = workspace.getOutputSchema();
		String inputSchema = workspace.getInputSchema();
		
		for (Entity entity : entities) {
			StringBuilder factSql = new StringBuilder();
			String outputTable = entity.getDataTable();
			String inputTable = entity.getDataTable();
			
			factSql.append( "CREATE TABLE %s.%s AS SELECT * ");
			
			//ALTER TABLE distributors ADD CONSTRAINT distfk FOREIGN KEY (address) REFERENCES addresses (address) MATCH FULL;

			//StringBuilder appendConstraints = new StringBuilder();
			List<Variable> variables = entity.getVariables();
			List<String> foreignKeys = new ArrayList<String>();
			
			for (Variable variable : variables) {
				if (!tableColumnExists(inputSchema, inputTable, variable.getValueColumn())) {
					factSql.append(", CAST( NULL as integer) AS ").append(variable.getValueColumn());
					
				}
				String sqlAlter = "ALTER TABLE %s.%s ADD CONSTRAINT %sfk FOREIGN KEY (%s) REFERENCES %s  ( %s ) MATCH FULL"; 
				
				foreignKeys.add(String.format(sqlAlter, outputSchema, outputTable, variable.getValueColumn(),
						variable.getValueColumn(), variable.getDimensionTable(), variable.getValueColumn()));
			}
			factSql.append( "  FROM %s.%s " ); 
			
			executeSql(factSql.toString(), outputSchema, outputTable, inputSchema, inputTable);

			for (String foreignKeyCommand : foreignKeys) {
				executeSql(foreignKeyCommand);
			}
		}

	}


	private boolean tableColumnExists(String inputSchema, String inputTable, String valueColumn) {
		String sql = "SELECT column_name FROM information_schema.columns WHERE table_name ='%s' AND column_name='%s' AND table_schema='%s'";
		
		sql = String.format(sql, inputTable, valueColumn, inputSchema );
		List<String> strList = getJdbcTemplate().query(sql, new RowMapper<String>() {
			public String mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
				return rs.getString(1);
			}
		});
		
		return strList.size() > 0;
	}

	private void convertCoordinatesToPoint() {

	}

	@Override
	protected void execute() throws Throwable {
		dropOutputSchema();
		createOutputSchema();
		createTables();

	}


	
}
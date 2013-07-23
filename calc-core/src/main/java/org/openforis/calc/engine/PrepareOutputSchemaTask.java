package org.openforis.calc.engine;

import java.util.List;

import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
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
	JdbcTemplate jdbcTemplate;


	private void dropOutputSchema() {
		String outputSchema = getContext().getWorkspace().getOutputSchema();
		String sql = "DROP SCHEMA IF EXISTS " + outputSchema + " CASCADE";
		jdbcTemplate.execute(sql);

	}

	private void createOutputSchema() {
		String outputSchema = getContext().getWorkspace().getOutputSchema();
		String sql = "CREATE SCHEMA " + outputSchema;

		jdbcTemplate.execute(sql);

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
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy aoiHierarchy : aoiHierarchies) {
			List<AoiHierarchyLevel> levels = aoiHierarchy.getLevels();
			for (AoiHierarchyLevel aoiHierarchyLevel : levels) {

				String sql = "CREATE TABLE %s.%s AS SELECT * FROM calc.aoi WHERE aoi_level_id = %d";
				String outputSchema = workspace.getOutputSchema();
				String tableName = aoiHierarchy.getName() + "_" + aoiHierarchyLevel.getName() + "_aoi_dim";
				tableName = fixTableName(tableName);
				Integer varId = aoiHierarchyLevel.getId();

				sql = String.format(sql, outputSchema, tableName, varId);
				jdbcTemplate.execute(sql);

				sql = "ALTER TABLE %s.%s ADD PRIMARY KEY (id)";
				sql = String.format(sql, outputSchema, tableName);
				jdbcTemplate.execute(sql);
			}
		}
	}

	private void createCategoricalDimensionTables(Workspace workspace) {
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();
			for (Variable variable : variables) {
				if (variable instanceof CategoricalVariable || variable instanceof BinaryVariable) {
					CategoricalVariable categoricalVariable = (CategoricalVariable) variable;
					String sql = "CREATE TABLE %s.%s AS SELECT * FROM calc.category WHERE variable_id = %d";
					String outputSchema = workspace.getOutputSchema();
					String tableName = entity.getName() + "_" + categoricalVariable.getName() + "_dim";
					tableName = fixTableName(tableName);
					Integer varId = variable.getId();

					sql = String.format(sql, outputSchema, tableName, varId);

					jdbcTemplate.execute(sql);

					sql = "ALTER TABLE %s.%s ADD PRIMARY KEY (id)";
					sql = String.format(sql, outputSchema, tableName);
					jdbcTemplate.execute(sql);
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
			
			//StringBuilder appendConstraints = new StringBuilder();
			List<Variable> variables = entity.getVariables();
			for (Variable variable : variables) {
				if (!tableColumnExists(inputSchema, inputTable, variable.getValueColumn())) {
					factSql.append(", CAST( NULL as integer) AS ").append(variable.getValueColumn());
				}

				//				if( variable instanceof BinaryVariable || variable instanceof CategoricalVariable ){
				//					appendConstraints.append( "CONSTRAINT ").append( inputTable).append( variable.getName() ).append("_fkey FOREIGN KEY (").append( variable.getValueColumn() ).append(") REFERENCES calc.workspace (id) " );
				//				}
			}
			factSql.append( "  FROM %s.%s " ); 
			
			String sql = String.format(factSql.toString(), outputSchema, outputTable, inputSchema, inputTable );

			jdbcTemplate.execute(sql);
		}

	}


	private boolean tableColumnExists(String inputSchema, String inputTable, String valueColumn) {
		String sql = "SELECT column_name FROM information_schema.columns WHERE table_name ='%s' AND column_name='%s' AND table_schema='%s'";
		
		sql = String.format(sql, inputTable, valueColumn, inputSchema );
		List<String> strList = jdbcTemplate.query(sql, new RowMapper<String>() {
			public String mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
				return rs.getString(1);
			}
		});
		
		return strList.size() > 0;
	}

	private String fixTableName(String tableName) {
		return tableName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
	}

	private void convertCoordinatesToPoint() {

	}

	@Override
	protected void execute() throws Throwable {
		jdbcTemplate = new JdbcTemplate(getContext().getDataSource());
		dropOutputSchema();
		createOutputSchema();
		createTables();

	}

	@Override
	protected long countTotalItems() {
		// TODO Auto-generated method stub
		return super.countTotalItems();
	}

	@Override
	public long getItemsProcessed() {
		// TODO Auto-generated method stub
		return super.getItemsProcessed();
	}
	
}
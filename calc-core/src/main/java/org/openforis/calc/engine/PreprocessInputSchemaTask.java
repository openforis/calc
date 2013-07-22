package org.openforis.calc.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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
public final class PreprocessInputSchemaTask extends Task {
	
	Logger logger = LoggerFactory.getLogger(PreprocessInputSchemaTask.class);

	private void dropOutputSchema() {
		String outputSchema = getContext().getWorkspace().getOutputSchema();
		// 

		if (schemaExists(outputSchema)) {
			String sql = "DROP SCHEMA " + outputSchema + " CASCADE";

			JdbcTemplate jdbcTemplate = new JdbcTemplate(getContext().getDataSource());

			jdbcTemplate.execute(sql);
		}

	}

	private boolean schemaExists(String outputSchema) {
		String sql = "select exists (select * from pg_catalog.pg_namespace where nspname ='?')";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(getContext().getDataSource());

		Boolean exists = jdbcTemplate.queryForObject(sql, new Object[] { outputSchema }, Boolean.class);
		
		return exists;
	}

	private void createOutputSchema() {

	}

	private void copyCodeListTables() {

	}

	private void copyTablesAddingVariables() {

	}

	private void convertCoordinatesToPoint() {

	}

	@Override
	protected void execute() throws Throwable {
		System.out.println("SDFSDF");
		dropOutputSchema();

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
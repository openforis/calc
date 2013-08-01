package org.openforis.calc.chain.pre;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.springframework.jdbc.core.RowMapper;

/**
 * Creates fact tables in output schema based on {@link Category}s
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz
 */
public final class CreateFactTablesTask extends SqlTask {
	
//	private void createFactTables() {
//		Workspace workspace = getWorkspace();
//		List<Entity> entities = workspace.getEntities();
//		String outputSchema = workspace.getOutputSchema();
//		String inputSchema = workspace.getInputSchema();
//		
//		for (Entity entity : entities) {
//			StringBuilder factSql = new StringBuilder();
//			String outputTable = entity.getDataTable();
//			String inputTable = entity.getDataTable();
//
//			factSql.append("CREATE TABLE %s.%s AS SELECT * ");
//
//			//ALTER TABLE distributors ADD CONSTRAINT distfk FOREIGN KEY (address) REFERENCES addresses (address) MATCH FULL;
//
//			//StringBuilder appendConstraints = new StringBuilder();
//			List<Variable> variables = entity.getVariables();
//			List<String> foreignKeys = new ArrayList<String>();
//
//			for (Variable variable : variables) {
//				if (!tableColumnExists(inputSchema, inputTable, variable.getValueColumn())) {
//					factSql.append(", CAST( NULL as integer) AS ").append(variable.getValueColumn());
//
//				}
//				// String sqlAlter = "ALTER TABLE %s.%s ADD CONSTRAINT %sfk FOREIGN KEY (%s) REFERENCES %s  ( %s ) MATCH FULL";
//
//				//				foreignKeys.add(String.format(sqlAlter, outputSchema, outputTable, variable.getValueColumn(),
//				//						variable.getValueColumn(), variable.getDimensionTable(), variable.getValueColumn()));
//			}
//			factSql.append("  FROM %s.%s ");
//
//			executeSql(factSql.toString(), outputSchema, outputTable, inputSchema, inputTable);
//
//			addPrimaryKeys(inputSchema, inputTable, outputSchema, outputTable);
//
//			log().debug("Fact table created:"+ outputTable);
//			for (String foreignKeyCommand : foreignKeys) {
//				executeSql(foreignKeyCommand);
//			}
//		}
//
//	}
//	
//	private void addPrimaryKeys(String inputSchema, String inputTable, String outputSchema, String outputTable) {
//		// TODO replace using column in Entity metadata
//		String getPkSql = "SELECT pg_attribute.attname FROM pg_index, pg_class, pg_attribute WHERE" +
// " pg_class.oid = '%s.%s'::regclass AND indrelid = pg_class.oid AND pg_attribute.attrelid = pg_class.oid AND pg_attribute.attnum = any(pg_index.indkey)"
//				+
//				" AND indisprimary";
//		
//		getPkSql = String.format(getPkSql, inputSchema, inputTable);
//		
//		List<String> primaryKeys = getJdbcTemplate().query(getPkSql, new RowMapper<String>() {
//			public String mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
//				return rs.getString(1);
//			}
//		}); 
//
//		for (String pk : primaryKeys) {
//			executeSql("ALTER TABLE %s.%s ADD PRIMARY KEY (%s)", outputSchema, outputTable, pk);
//		}
//	}
//	
//	private boolean tableColumnExists(String inputSchema, String inputTable, String valueColumn) {
//		String sql = "SELECT column_name FROM information_schema.columns WHERE table_name ='%s' AND column_name='%s' AND table_schema='%s'";
//
//		sql = String.format(sql, inputTable, valueColumn, inputSchema);
//		List<String> strList = getJdbcTemplate().query(sql, new RowMapper<String>() {
//			public String mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
//				return rs.getString(1);
//			}
//		});
//
//		return strList.size() > 0;
//	}
//
//
//	@Override
//	protected void execute() throws Throwable {
//		createFactTables();
//	}
}
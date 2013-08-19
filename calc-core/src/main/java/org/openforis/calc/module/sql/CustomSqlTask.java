package org.openforis.calc.module.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.openforis.calc.engine.CalculationStepTask;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.PsqlBuilder;


/**
 * Runs a user-defined SQL statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomSqlTask extends CalculationStepTask {

	@Override
	protected void execute() throws SQLException {
		ParameterMap params = parameters();
		String sql = params.getString("sql");
		DataSource ds = getDataSource();
		Workspace workspace = getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		log().debug("Executing custom SQL");
		new PsqlBuilder(ds).setSchemaSearchPath(outputSchema, PsqlBuilder.PUBLIC).execute();
		new PsqlBuilder(ds).appendSql(sql).execute();
	}
}
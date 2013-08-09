package org.openforis.calc.module.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.openforis.calc.engine.CalculationStepTask;
import org.openforis.calc.engine.JobContext;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.Psql;


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
		JobContext context = getContext();
		DataSource ds = context.getDataSource();
		Workspace workspace = context.getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		log().debug("Executing custom SQL");
		new Psql(ds).setSchemaSearchPath(outputSchema, Psql.PUBLIC).execute();
		new Psql(ds).appendSql(sql).execute();
	}
}
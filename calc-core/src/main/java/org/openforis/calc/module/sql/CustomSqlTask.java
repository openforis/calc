package org.openforis.calc.module.sql;

import java.sql.SQLException;

import org.openforis.calc.engine.CalculationStepTask;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.OutputSchema;


/**
 * Runs a user-defined SQL statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomSqlTask extends CalculationStepTask {

	@Override
	protected void execute() throws SQLException {
		String sql = getCalculationStep().getScript();
		OutputSchema outputSchema = getOutputSchema();
		psql().setDefaultSchemaSearchPath(outputSchema, Psql.PUBLIC).execute();		
		psql().execute(sql);
	}
}
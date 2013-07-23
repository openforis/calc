package org.openforis.calc.module.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.openforis.calc.chain.CalculationStepTask;
import org.openforis.calc.engine.JobContext;
import org.openforis.calc.engine.ParameterMap;


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
		log().info("Executing custom SQL: "+sql);
		JobContext context = getContext();
		DataSource ds = context.getDataSource();
		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		
		
//		//MINO
//		try {
//			Thread.sleep(5000);
//		} catch ( InterruptedException e ) {
////			e.printStackTrace();
//		}
	}
}
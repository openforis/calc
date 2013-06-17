package org.openforis.calc.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


/**
 * Runs a user-defined SQL statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomSqlTask extends Task {
	
	@Override
	protected void execute() throws SQLException {
		ParameterMap params = getParameters();
		String sql = params.getString("sql");
		log().info("Executing custom SQL: "+sql);
		Context context = getContext();
		DataSource ds = context.getDataSource();
		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		if ( res.next() ) {
			System.out.println(res.getInt(1));
		}
	}
}
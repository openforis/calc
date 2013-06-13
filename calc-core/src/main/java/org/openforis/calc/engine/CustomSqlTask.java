package org.openforis.calc.engine;


/**
 * Runs a user-defined SQL statement or script.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CustomSqlTask extends Task {
	
	public CustomSqlTask() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected boolean execute() {
		ParameterMap params = getParameters();
		String sql = params.getString("sql");
		log().info("Executing custom SQL: "+sql);
		Context context = getContext();
//		DataSource ds = context.getDataSource();
//		ds.getConnection();
		// TODO Auto-generated method stub
		return true;
	}
}
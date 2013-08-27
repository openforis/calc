package org.openforis.calc.psql;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.impl.DefaultExecuteListener;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
class LogSqlListener extends DefaultExecuteListener implements ExecuteListener {
	private static final long serialVersionUID = 1L;
	
	private Psql psql;
	
	public LogSqlListener(Psql psql) {
		this.psql = psql;
	}

	@Override
	public void renderEnd(ExecuteContext ctx) {
		String sql = ctx.sql();
		psql.logSql(sql);
	}
}

package org.openforis.calc.psql;

import org.apache.commons.lang3.StringUtils;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.impl.DefaultExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author G. Miceli
 * @author Mino Togna
 * @author S. Ricci
 *
 */
class LogSqlListener extends DefaultExecuteListener implements ExecuteListener {
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger( LogSqlListener.class );
	
	@Override
	public void renderEnd(ExecuteContext ctx) {
		String sql = ctx.sql();
		if ( sql == null ) {
			//try to see if context contains batch sql
			String[] batchSQL = ctx.batchSQL();
			if ( batchSQL != null ) {
				logText("Execute batch sql");
			}
		}
		logSql(sql);
	}
	
	void logSql(String sql, Object... bindings) {
		if (bindings.length == 0) {
			log.debug(sql + ";");
		} else {
			log.debug(sql + "; -- Parameters: " + StringUtils.join(bindings) + "");
		}
	}
	
	void logText(String text) {
		log.debug(text);
	}
}

/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

/**
 * This class transforms SQLException into a Spring specific DataAccessException. The idea behind this is borrowed from Adam Zell's Gist
 * 
 * <i>based on ExceptionTranslator privded by Jooq examples project
 * https://github.com/jOOQ/jOOQ/blob/master/jOOQ-examples/jOOQ-spring-example/src/main/java/org/jooq/example/spring/exception/ExceptionTranslator.java</i>
 * 
 * @author Mino Togna
 * @author S. Ricci
 */
public class ExceptionTranslator extends DefaultExecuteListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void exception(ExecuteContext ctx) {
		SQLDialect dialect = ctx.configuration().dialect();
		SQLExceptionTranslator translator = 
				(dialect != null) 
				? new SQLErrorCodeSQLExceptionTranslator(dialect.name()) 
				: new SQLStateSQLExceptionTranslator();

		ctx.exception(translator.translate("jOOQ", ctx.sql(), ctx.sqlException()));
	}
}

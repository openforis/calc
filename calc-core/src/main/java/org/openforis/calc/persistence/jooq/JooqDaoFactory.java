package org.openforis.calc.persistence.jooq;

import java.sql.Connection;

import org.jooq.DAO;
import org.jooq.impl.Factory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class JooqDaoFactory extends JdbcDaoSupport {
	public Factory getJooqFactory() {
		Connection connection = getConnection();
		return new DialectAwareJooqFactory(connection);
	}

	
	// TODO createJooqDao()
}

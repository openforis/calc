package org.openforis.calc.persistence.jooq;

import java.sql.Connection;

import org.jooq.DAO;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DAOImpl;
import org.jooq.impl.Factory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class JooqDaoSupport extends JdbcDaoSupport {
	protected Factory createJooqFactory() {
		Connection connection = getConnection();
		return new DialectAwareJooqFactory(connection);
	}

	protected <R extends TableRecord<R>,P,T> DAO<R, P, T> createJooqDao(Table<R> table, Class<P> type) {
		Factory factory = createJooqFactory();
		return new DAOImpl<R, P, T>(table, type, factory) {
			@Override
			protected T getId(P object) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
	
	// TODO createJooqDao()
}

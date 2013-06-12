package org.openforis.calc.persistence.jooq;

import java.sql.Connection;

import org.jooq.Table;
import org.jooq.TableRecord;
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

	private Factory createJooqFactory() {
		Connection connection = getConnection();
		return new DialectAwareJooqFactory(connection);
	}

	public <R extends TableRecord<R>, P, T> JooqDao<R, P, T> createJooqDao(Table<R> table, Class<P> entityType) {
		Factory factory = createJooqFactory();
		return new JooqDao<R, P, T>(table, entityType, factory);
	}

	public <R extends TableRecord<R>, P, T> JooqDao<R, P, T> createJooqDao(Class<P> entityType) {
		Factory factory = createJooqFactory();
		Table<R> table = getTableFromJpaAnnotations(entityType);
		return new JooqDao<R, P, T>(table, entityType, factory);
	}

	@SuppressWarnings("unchecked")
	private <R extends TableRecord<R>, P> Table<R> getTableFromJpaAnnotations(Class<P> entityType) {
		javax.persistence.Table tableAnn = entityType.getAnnotation(javax.persistence.Table.class);
		String schemaName = tableAnn.schema();
		String tableName = tableAnn.name();
		Table<R> table = (Table<R>) Factory.tableByName(schemaName, tableName);
		return table;
	}
}

package org.openforis.calc.persistence.jooq;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
		DialectAwareJooqFactory factory = new DialectAwareJooqFactory(connection);
		Settings settings = factory.getSettings();
		settings.setRenderNameStyle(RenderNameStyle.QUOTED);
		return factory;
	}
	
	/**
	 * Workaround required to set the dataSource. Why? 
	 * @param dataSource
	 */
	@Autowired(required = true)
	@Qualifier("dataSource")
	private void setDataSourceInternal(DataSource dataSource) {
		setDataSource(dataSource);
	}

	public <R extends TableRecord<R>, P, T> JooqDao<R, P, T> createJooqDao(Table<R> table, Class<P> entityType) {
		Factory factory = createJooqFactory();
		return new JooqDao<R, P, T>(table, entityType, factory);
	}

	public <R extends TableRecord<R>, P, T> JooqDao<R, P, T> createJooqDao(Class<P> entityType) {
		Factory factory = createJooqFactory();
		Table<R> table = getJooqTable(entityType);
		return new JooqDao<R, P, T>(table, entityType, factory);
	}

	@SuppressWarnings("unchecked")
	private <R extends TableRecord<R>, P> Table<R> getJooqTable(Class<P> entityType) {
		javax.persistence.Table tableAnn = entityType.getAnnotation(javax.persistence.Table.class);
		String tableName = tableAnn.name();
//		String schemaName = tableAnn.schema();
//		Table<R> table = (Table<R>) Factory.tableByName(schemaName, tableName);
		Table<R> table = (Table<R>) CalcSchema.CALC.getTable(tableName);
		return table;
	}
}

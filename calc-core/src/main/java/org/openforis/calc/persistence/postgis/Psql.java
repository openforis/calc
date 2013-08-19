package org.openforis.calc.persistence.postgis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Simple PostreSQL query builder
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public final class Psql extends DefaultDSLContext {
	private static final long serialVersionUID = 1L;
	public enum Privilege {ALL, SELECT};
	
	public Psql() {
		super(SQLDialect.POSTGRES);
	}
	
	public Psql(DataSource dataSource) {
		super(DataSourceUtils.getConnection(dataSource), SQLDialect.POSTGRES);
	}
	
	// New DSL-based PSQL class

	public GrantStep grant(Privilege... privileges) {
		return new GrantStep(this, privileges);
	}
}

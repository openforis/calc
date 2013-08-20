package org.openforis.calc.persistence.postgis;

import javax.sql.DataSource;

import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
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

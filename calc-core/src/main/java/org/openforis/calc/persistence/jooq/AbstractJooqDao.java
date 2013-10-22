/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import javax.sql.DataSource;

import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Mino Togna
 * 
 */
@Repository
public abstract class AbstractJooqDao {

	@Autowired
	private DataSource dataSource;

	protected Psql psql() {
		return new Psql(dataSource);
	}

}

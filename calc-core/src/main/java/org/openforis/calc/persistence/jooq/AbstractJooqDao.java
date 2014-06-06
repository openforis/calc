/**
 * 
 */
package org.openforis.calc.persistence.jooq;

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
	private Psql psql;
	
	protected Psql psql() {
		return psql;
	}

}

package org.openforis.calc.persistence;

import org.openforis.calc.persistence.jooq.JooqDaoSupport;


/**
 * @author G. Miceli
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractDynamicJooqDao extends JooqDaoSupport {

	@SuppressWarnings("unchecked")
	protected AbstractDynamicJooqDao() {
		super(null, null);
	}

}

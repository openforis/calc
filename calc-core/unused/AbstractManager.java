package org.openforis.calc.persistence;

import org.jooq.TableRecord;
import org.openforis.calc.common.Identifiable;
import org.openforis.calc.persistence.jooq.JooqDao;
import org.openforis.calc.persistence.jooq.JooqDaoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manages persistence and creation of persistable instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Component
public abstract class AbstractManager<P extends Identifiable> {
	@Autowired
	private JooqDaoFactory daoFactory;
	private Class<P> objectType;

	protected AbstractManager(Class<P> objectType) {
		this.objectType = objectType;
	}
	
//	protected Class<P> getObjectType() {
//		return objectType;
//	}

	protected <R extends TableRecord<R>> JooqDao<R, P, Integer> createJooqDao() {
		return daoFactory.createJooqDao(objectType);
	}
}
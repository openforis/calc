/**
 * 
 */
package org.openforis.calc.persistence;

import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * 
 */
@SuppressWarnings("rawtypes")
@Component
@Transactional
public class OlapSchemaDao extends JooqDaoSupport {

	@SuppressWarnings("unchecked")
	public OlapSchemaDao() {
		super(null, null);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void dropSchema(String schema) {
		String sql = "drop schema if exists "+schema+" cascade";
		executeSql(sql);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createSchema(String schema){
		String sql = "create schema "+schema;
		executeSql(sql);
	}
	
}

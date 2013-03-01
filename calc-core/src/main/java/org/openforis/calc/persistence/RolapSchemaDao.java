/**
 * 
 */
package org.openforis.calc.persistence;

import java.util.List;

import org.openforis.calc.persistence.jooq.DdlGenerator;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.RolapTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
@SuppressWarnings("rawtypes")
@Component
@Transactional
public class RolapSchemaDao extends JooqDaoSupport {
	@Autowired
	private DdlGenerator dllGenerator;

	@SuppressWarnings("unchecked")
	public RolapSchemaDao() {
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
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createTables(List<RolapTable> tables) {
		for (RolapTable table : tables) {
			dllGenerator.createTable(table);
		}
	}
}

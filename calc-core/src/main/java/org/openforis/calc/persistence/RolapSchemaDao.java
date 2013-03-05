/**
 * 
 */
package org.openforis.calc.persistence;

import java.util.List;

import org.openforis.calc.persistence.jooq.DdlGenerator;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
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
	public void createSchema(List<RolapTable> tables) {
		String schema = tables.get(0).getSchema().getName();
		if ( !isValidSchemaName(schema) ) {
			throw new IllegalArgumentException("Cannot overwrite calc database schema");
		}
		for (RolapTable table : tables) {
			if ( !table.getSchema().getName().equals(schema) ) {
				throw new IllegalArgumentException("Tables must all belong to the same schema");
			}
		}
		
		String sql = "drop schema if exists "+schema+" cascade";
		executeSql(sql);
		
		sql = "create schema "+schema;
		executeSql(sql);
		
		for (RolapTable table : tables) {
			dllGenerator.createTable(table);
		}
	}

	public boolean isValidSchemaName(String schema) {
		return !schema.equalsIgnoreCase(Tables.SURVEY.getSchema().getName());
	}
	
}

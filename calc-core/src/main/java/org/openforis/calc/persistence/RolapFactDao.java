package org.openforis.calc.persistence;

import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.FactTable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author G. Miceli
 */
@SuppressWarnings("rawtypes")
@Component
@Transactional
public abstract class RolapFactDao<T extends FactTable> extends JooqDaoSupport {

	@SuppressWarnings("unchecked")
	public RolapFactDao() {
		super(null, null);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	synchronized
	public void populate(T table) {
		SelectQuery select = createFactSelect(table);
		Insert<Record> insert = createInsertFromSelect(table, select);
		
		getLog().debug("Inserting fact data:");
		getLog().debug(insert);
		
		insert.execute();
		
		getLog().debug("Complete");
	}

	protected abstract SelectQuery createFactSelect(T table);
	
}

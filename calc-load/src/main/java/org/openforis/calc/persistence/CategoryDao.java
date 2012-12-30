package org.openforis.calc.persistence;

import org.openforis.calc.model.Category;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.CategoryRecord;

/**
 * @author G. Miceli
 */
public class CategoryDao extends JooqDaoSupport<CategoryRecord, Category> {

	public CategoryDao() {
		super(Tables.CATEGORY, Category.class);
	}

}

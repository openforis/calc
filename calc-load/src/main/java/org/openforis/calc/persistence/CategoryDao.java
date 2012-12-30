package org.openforis.calc.persistence;

import org.openforis.calc.model.Category;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.CategoryRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class CategoryDao extends JooqDaoSupport<CategoryRecord, Category> {

	public CategoryDao() {
		super(Tables.CATEGORY, Category.class);
	}

}

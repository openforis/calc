package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import java.util.List;

import org.openforis.calc.model.Category;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.CategoryRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class CategoryDao extends JooqDaoSupport<CategoryRecord, Category> {

	public CategoryDao() {
		super(CATEGORY, Category.class);
	}

	public List<Category> findByVariableId(int id) {
		return fetch(CATEGORY.VARIABLE_ID, id);
	}

}

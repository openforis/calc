package org.openforis.calc.persistence;

import org.openforis.calc.model.SpecimenCategory;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenCategoryRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class SpecimenCategoryDao extends JooqDaoSupport<SpecimenCategoryRecord, SpecimenCategory> {

	public SpecimenCategoryDao() {
		super(Tables.SPECIMEN_CATEGORY, SpecimenCategory.class);
	}
}
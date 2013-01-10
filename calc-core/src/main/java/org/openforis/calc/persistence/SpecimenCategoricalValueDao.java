package org.openforis.calc.persistence;

import org.openforis.calc.model.SpecimenCategoricalValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenCategoricalValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class SpecimenCategoricalValueDao extends JooqDaoSupport<SpecimenCategoricalValueRecord, SpecimenCategoricalValue> {

	public SpecimenCategoricalValueDao() {
		super(Tables.SPECIMEN_CATEGORICAL_VALUE, SpecimenCategoricalValue.class);
	}

}

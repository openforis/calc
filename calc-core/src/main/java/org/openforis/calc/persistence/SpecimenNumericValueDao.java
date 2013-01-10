package org.openforis.calc.persistence;

import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenNumericValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class SpecimenNumericValueDao extends JooqDaoSupport<SpecimenNumericValueRecord, SpecimenNumericValue> {

	public SpecimenNumericValueDao() {
		super(Tables.SPECIMEN_NUMERIC_VALUE, SpecimenNumericValue.class);
	}
}
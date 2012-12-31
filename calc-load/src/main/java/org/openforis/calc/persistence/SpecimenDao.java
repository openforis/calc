package org.openforis.calc.persistence;

import org.openforis.calc.model.Specimen;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class SpecimenDao extends JooqDaoSupport<SpecimenRecord, Specimen> {

	public SpecimenDao() {
		super(Tables.SPECIMEN, Specimen.class);
	}
}
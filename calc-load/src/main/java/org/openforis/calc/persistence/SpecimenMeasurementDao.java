package org.openforis.calc.persistence;

import org.openforis.calc.model.SpecimenMeasurement;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenMeasurementRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class SpecimenMeasurementDao extends JooqDaoSupport<SpecimenMeasurementRecord, SpecimenMeasurement> {

	public SpecimenMeasurementDao() {
		super(Tables.SPECIMEN_MEASUREMENT, SpecimenMeasurement.class);
	}
}
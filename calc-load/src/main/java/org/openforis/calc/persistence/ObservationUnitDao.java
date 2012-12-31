package org.openforis.calc.persistence;

import java.util.List;

import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.ObservationUnitRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class ObservationUnitDao extends JooqDaoSupport<ObservationUnitRecord, ObservationUnit> {

	public ObservationUnitDao() {
		super(Tables.OBSERVATION_UNIT, ObservationUnit.class);
	}

	public List<ObservationUnit> fetchBySurveyId(int surveyId) {
		return fetch(Tables.OBSERVATION_UNIT.SURVEY_ID, surveyId);
	}
}

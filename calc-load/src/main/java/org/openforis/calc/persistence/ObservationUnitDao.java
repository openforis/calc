package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import java.util.List;

import org.jooq.Record;
import org.jooq.impl.Factory;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.ObservationUnitRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class ObservationUnitDao extends JooqDaoSupport<ObservationUnitRecord, ObservationUnit> {

	public ObservationUnitDao() {
		super(OBSERVATION_UNIT, ObservationUnit.class);
	}

	public List<ObservationUnit> findBySurveyId(int surveyId) {
		return fetch(OBSERVATION_UNIT.SURVEY_ID, surveyId);
	}

	public ObservationUnit find(Integer surveyId, String type, String name) {
		Factory create = getJooqFactory();
		Record record = create.select()
				     .from(OBSERVATION_UNIT)
				     .where(OBSERVATION_UNIT.SURVEY_ID.eq(surveyId)
				    		 .and(OBSERVATION_UNIT.TYPE.eq(type))
				    		 .and(OBSERVATION_UNIT.NAME.eq(name)))
				     .fetchOne();
		return record == null ? null : record.into(ObservationUnit.class);
	}
}

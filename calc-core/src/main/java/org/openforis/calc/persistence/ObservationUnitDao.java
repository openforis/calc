package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.OBSERVATION_UNIT;

import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.Factory;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.ObservationUnitRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class ObservationUnitDao extends JooqDaoSupport<ObservationUnitRecord, ObservationUnit> {

	public ObservationUnitDao() {
		super(OBSERVATION_UNIT, ObservationUnit.class, OBSERVATION_UNIT.SURVEY_ID, OBSERVATION_UNIT.OBS_UNIT_NAME);
	}

	public List<ObservationUnit> findBySurveyId(int surveyId) {
		return fetch(OBSERVATION_UNIT.SURVEY_ID, surveyId);
	}

	public ObservationUnit find(int surveyId, String name) {
		Factory create = getJooqFactory();
		Record record = create.select()
				     .from(OBSERVATION_UNIT)
				     .where(OBSERVATION_UNIT.SURVEY_ID.eq(surveyId)
				    		 .and(OBSERVATION_UNIT.OBS_UNIT_NAME.eq(name)))
				     .fetchOne();
		return record == null ? null : record.into(ObservationUnit.class);
	}

	public FlatDataStream streamAll(String[] fieldNames, int surveyId) {
		Field<?>[] fields = getFields(fieldNames);
		return stream(fields, OBSERVATION_UNIT.SURVEY_ID, surveyId);
	}

	public FlatDataStream streamByName(String[] fieldNames, int surveyId, String name) {
		Field<?>[] fields = getFields(fieldNames);
		Result<Record> result = selectByName(surveyId, name, fields).fetch();
		return stream(result);
	}

//	public Integer findIdByName(int surveyId, String name) {
//		return selectByName(surveyId, name, OBSERVATION_UNIT.ID).fetchOne(SURVEY.ID);
//	}
	
	private SelectConditionStep selectByName(int surveyId, String name, Field<?>... fields) {
		Factory create = getJooqFactory();
		return create.select(fields)
				     .from(OBSERVATION_UNIT)
				     .where(OBSERVATION_UNIT.SURVEY_ID.eq(surveyId)
				    		 .and(OBSERVATION_UNIT.OBS_UNIT_NAME.eq(name)));		
	}

	public Integer getId(int surveyId, String key) {
		return getIdByKey(surveyId, key);
	}
}

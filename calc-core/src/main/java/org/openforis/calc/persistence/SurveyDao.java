package org.openforis.calc.persistence;

import org.jooq.Result;
import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import static org.openforis.calc.persistence.jooq.Tables.*;
import org.openforis.calc.persistence.jooq.tables.records.SurveyRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class SurveyDao extends JooqDaoSupport<SurveyRecord, Survey> {

	public SurveyDao() {
		super(SURVEY, Survey.class);
	}
	
	public Survey findByUri(String uri) {
		return fetchOne(SURVEY.URI, uri);
	}

	public Survey findByName(String name) {
		return fetchOne(SURVEY.NAME, name);
	}

	public Result<SurveyRecord> fetchAll() {
		return getJooqFactory().fetch(SURVEY);
	}

	public Result<SurveyRecord> fetchByName(String name) {
		return getJooqFactory().fetch(SURVEY, SURVEY.NAME.eq(name));
	}

}

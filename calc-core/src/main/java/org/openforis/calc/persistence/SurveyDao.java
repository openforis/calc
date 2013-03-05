package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SURVEY;

import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SurveyRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class SurveyDao extends JooqDaoSupport<SurveyRecord, Survey> {

	public SurveyDao() {
		super(SURVEY, Survey.class, SURVEY.SURVEY_NAME);
	}
	
	public Survey findByUri(String uri) {
		return fetchOne(SURVEY.SURVEY_URI, uri);
	}

	public Survey findByName(String name) {
		return fetchOne(SURVEY.SURVEY_NAME, name);
	}

	public Integer getId(String name) {
		return getIdByKey(name);
	}
	
	public FlatDataStream streamByName(String[] fieldNames, String name) {
		return stream(fieldNames, SURVEY.SURVEY_NAME, name);
	}		

}

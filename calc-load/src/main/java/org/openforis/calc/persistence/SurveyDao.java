package org.openforis.calc.persistence;

import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SurveyRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class SurveyDao extends JooqDaoSupport<SurveyRecord, Survey> {

	public SurveyDao() {
		super(Tables.SURVEY, Survey.class);
	}

}

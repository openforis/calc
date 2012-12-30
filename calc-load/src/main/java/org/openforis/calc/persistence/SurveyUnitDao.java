package org.openforis.calc.persistence;

import org.openforis.calc.model.SurveyUnit;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SurveyUnitRecord;

/**
 * @author G. Miceli
 */
public class SurveyUnitDao extends JooqDaoSupport<SurveyUnitRecord, SurveyUnit> {

	public SurveyUnitDao() {
		super(Tables.SURVEY_UNIT, SurveyUnit.class);
	}

}

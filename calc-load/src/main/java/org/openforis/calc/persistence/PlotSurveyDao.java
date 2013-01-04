package org.openforis.calc.persistence;

import org.openforis.calc.model.PlotSurvey;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.PlotSurveyRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class PlotSurveyDao extends JooqDaoSupport<PlotSurveyRecord, PlotSurvey> {

	public PlotSurveyDao() {
		super(Tables.PLOT_SURVEY, PlotSurvey.class);
	}
}
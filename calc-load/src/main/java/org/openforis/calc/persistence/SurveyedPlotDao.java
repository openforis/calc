package org.openforis.calc.persistence;

import org.openforis.calc.model.SurveyedPlot;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SurveyedPlotRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class SurveyedPlotDao extends JooqDaoSupport<SurveyedPlotRecord, SurveyedPlot> {

	public SurveyedPlotDao() {
		super(Tables.SURVEYED_PLOT, SurveyedPlot.class);
	}
}
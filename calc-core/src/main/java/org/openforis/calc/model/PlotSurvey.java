package org.openforis.calc.model;

import java.util.Date;

/**
 * @author G. Miceli
 */
public class PlotSurvey extends org.openforis.calc.persistence.jooq.tables.pojos.PlotSurvey implements ObservationUnitInstance {

	private static final long serialVersionUID = 1L;

	public void setSurveyDate(Date surveyDate) {
		super.setSurveyDate(surveyDate == null ? null : new java.sql.Date(surveyDate.getTime()));
	}
}

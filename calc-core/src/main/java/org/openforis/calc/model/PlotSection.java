package org.openforis.calc.model;

import java.util.Date;

/**
 * @author G. Miceli
 */
public class PlotSection extends org.openforis.calc.persistence.jooq.tables.pojos.PlotSection implements Identifiable {

	private static final long serialVersionUID = 1L;

	public void setSurveyDate(Date surveyDate) {
		super.setPlotSectionSurveyDate(surveyDate == null ? null : new java.sql.Date(surveyDate.getTime()));
	}

	@Override
	public Integer getId() {
		return super.getPlotSectionId();
	}

	@Override
	public void setId(Integer id) {
		super.setPlotSectionId(id);
	}
}

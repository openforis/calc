package org.openforis.calc.model;

import java.util.Date;

/**
 * @author G. Miceli
 */
public class PlotSection extends org.openforis.calc.persistence.jooq.tables.pojos.PlotSection implements Observation {

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
	
	public static String getPlotIdentifer(String clusterCode, Integer plotNo, String plotSection, String surveyType) {
		StringBuilder sb = new StringBuilder();
		sb.append(clusterCode);
		sb.append(" ");
		sb.append(plotNo);
		if ( plotSection != null ) {
			sb.append(" ");
			sb.append(plotSection);
		}
		if ( !"P".equals(surveyType) ) { 
			sb.append(" (");
			sb.append(surveyType);
			sb.append(")");
		}
		return sb.toString();
	}
}

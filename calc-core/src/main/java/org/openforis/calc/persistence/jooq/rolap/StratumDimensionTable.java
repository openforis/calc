package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class StratumDimensionTable extends DimensionTable {
	private static final long serialVersionUID = 1L;

	private static final String TABLE_NAME = "stratum";

	private int surveyId;
    
    StratumDimensionTable(String schema, int surveyId) {
		super(schema, TABLE_NAME);
		this.surveyId = surveyId;
	}
    
    public int getSurveyId() {
		return surveyId;
	}
}

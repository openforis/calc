package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 *
 */
public class ClusterDimensionTable extends HierarchicalDimensionTable {
	private static final long serialVersionUID = 1L;

	private static final String TABLE_NAME = "cluster";

	private int surveyId;
    
    ClusterDimensionTable(String schema, int surveyId) {
		super(schema, TABLE_NAME, null);
		this.surveyId = surveyId;
	}
    
    public int getSurveyId() {
		return surveyId;
	}
}

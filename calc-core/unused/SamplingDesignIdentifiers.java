package org.openforis.calc.model;


/**
 * 
 * @author G. Miceli
 *
 */
public class SamplingDesignIdentifiers {
	private int surveyId;
	private IdentifierMap stratumIds;
	private IdentifierMap clusterIds;
	private PlotIdentifierMap plotIds;
	
	public SamplingDesignIdentifiers(int surveyId) {
		this.surveyId = surveyId;
	}

	public int getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(int surveyId) {
		this.surveyId = surveyId;
	}

	public IdentifierMap getStratumIds() {
		return stratumIds;
	}

	public void setStratumIds(IdentifierMap stratumIds) {
		this.stratumIds = stratumIds;
	}

	public IdentifierMap getClusterIds() {
		return clusterIds;
	}

	public void setClusterIds(IdentifierMap clusterIds) {
		this.clusterIds = clusterIds;
	}

	public PlotIdentifierMap getPlotIds() {
		return plotIds;
	}

	public void setPlotIds(PlotIdentifierMap plotIds) {
		this.plotIds = plotIds;
	}
}

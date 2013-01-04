package org.openforis.calc.model;

import org.openforis.calc.persistence.jooq.tables.pojos.PlotMeasurementBase;

/**
 * @author G. Miceli
 */
public class PlotMeasurement extends PlotMeasurementBase implements ModelObject {

	private static final long serialVersionUID = 1L;
	
	public PlotMeasurement() {
	}
	
	public PlotMeasurement(PlotSurvey plot, Variable var, Double value, boolean computed) {
		setPlotSurveyId(plot.getId());
		setVariableId(var.getId());
		setValue(value);
		setComputed(computed);
	}
}

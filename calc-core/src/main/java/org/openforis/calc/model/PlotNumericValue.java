package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class PlotNumericValue extends org.openforis.calc.persistence.jooq.tables.pojos.PlotNumericValue implements Identifiable {

	private static final long serialVersionUID = 1L;

	public PlotNumericValue() {
	}

	public PlotNumericValue(PlotSection plot, Variable var, Double value, boolean computed) {
		setPlotSectionId(plot.getId());
		setVariableId(var.getId());
		setValue(value);
		setComputed(computed);
	}

	public PlotNumericValue(PlotSection plot, int variableId, Double value, boolean computed) {
		setPlotSectionId(plot.getId());
		setVariableId(variableId);
		setValue(value);
		setComputed(computed);
	}

	@Override
	public Integer getId() {
		return super.getValueId();
	}

	@Override
	public void setId(Integer id) {
		super.setValueId(id);
	}
}

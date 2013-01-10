package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class PlotCategoricalValue extends org.openforis.calc.persistence.jooq.tables.pojos.PlotCategoricalValue implements Identifiable {

	private static final long serialVersionUID = 1L;

	public PlotCategoricalValue() {
	}

	public PlotCategoricalValue(PlotSection plot, Category cat, boolean computed) {
		setPlotSectionId(plot.getId());
		setCategoryId(cat.getId());
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

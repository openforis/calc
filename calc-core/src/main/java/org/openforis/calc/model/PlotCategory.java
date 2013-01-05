package org.openforis.calc.model;


/**
 * @author G. Miceli
 */
public class PlotCategory extends org.openforis.calc.persistence.jooq.tables.pojos.PlotCategory implements Identifiable {

	private static final long serialVersionUID = 1L;

	public PlotCategory() {
	}
	
	public PlotCategory(PlotSection plot, Category cat, boolean computed) {
		setPlotSectionId(plot.getId());
		setCategoryId(cat.getId());
		setComputed(computed);
	}
}

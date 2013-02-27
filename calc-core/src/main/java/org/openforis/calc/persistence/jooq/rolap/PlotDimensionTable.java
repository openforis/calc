package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
// TODO remove record classes?
public class PlotDimensionTable extends HierarchicalDimensionTable<PlotDimensionRecord> {

	private static final long serialVersionUID = 1L;

	private ObservationUnitMetadata observationUnitMetadata;
	PlotDimensionTable(String schema, ObservationUnitMetadata unit) {
		super(schema, unit.getObsUnitName(), PlotDimensionRecord.class);
		this.observationUnitMetadata = unit;
	}

	public ObservationUnitMetadata getObservationUnitMetadata() {
		return observationUnitMetadata;
	}
}
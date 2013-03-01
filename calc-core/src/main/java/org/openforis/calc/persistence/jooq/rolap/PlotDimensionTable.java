package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PlotDimensionTable extends HierarchicalDimensionTable {

	private static final long serialVersionUID = 1L;

	private ObservationUnitMetadata observationUnitMetadata;
	PlotDimensionTable(String schema, ObservationUnitMetadata unit, ClusterDimensionTable parentTable) {
		super(schema, unit.getObsUnitName(), parentTable);
		this.observationUnitMetadata = unit;
	}

	public ObservationUnitMetadata getObservationUnitMetadata() {
		return observationUnitMetadata;
	}
}
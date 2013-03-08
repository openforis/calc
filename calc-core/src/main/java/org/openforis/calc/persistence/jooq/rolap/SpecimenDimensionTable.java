package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author M. Togna
 * 
 */
public class SpecimenDimensionTable extends HierarchicalDimensionTable {

	private static final long serialVersionUID = 1L;

	private ObservationUnitMetadata observationUnitMetadata;

	SpecimenDimensionTable(String schema, ObservationUnitMetadata unit, PlotDimensionTable parentTable) {
		super(schema, unit.getObsUnitName(), parentTable);
		this.observationUnitMetadata = unit;
	}

	public ObservationUnitMetadata getObservationUnitMetadata() {
		return observationUnitMetadata;
	}
}

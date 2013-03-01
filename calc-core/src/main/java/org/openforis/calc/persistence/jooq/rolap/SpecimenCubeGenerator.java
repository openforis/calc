package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
 *
 */
public class SpecimenCubeGenerator extends CubeGenerator {

	SpecimenCubeGenerator(String dbSchema, ObservationUnitMetadata unit) {
		super(dbSchema, unit);
	}

	@Override
	protected FactTable createFactTable(List<String> measureColumns, List<String> dimColumns) {
		return new SpecimenFactTable(getDatabaseSchema(), getObservationUnitMetadata(), measureColumns, dimColumns);
	}

}

package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
 *
 */
public class InterviewCubeGenerator extends CubeGenerator {

	InterviewCubeGenerator(String dbSchema, ObservationUnitMetadata unit) {
		super(dbSchema, unit);
	}

	@Override
	protected FactTable createFactTable(List<String> measureColumns, List<String> dimColumns) {
		return new InterviewFactTable(getDatabaseSchema(), getObservationUnitMetadata(), measureColumns, dimColumns);
	}
}

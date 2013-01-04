package org.openforis.calc.model;

import org.openforis.calc.persistence.jooq.tables.pojos.SpecimenMeasurementBase;

/**
 * @author G. Miceli
 */
public class SpecimenMeasurement extends SpecimenMeasurementBase implements ModelObject {

	private static final long serialVersionUID = 1L;
	
	public SpecimenMeasurement() {
	}
	
	public SpecimenMeasurement(Specimen specimen, Variable var, Double value, boolean computed) {
		setSpecimenId(specimen.getId());
		setVariableId(var.getId());
		setValue(value);
		setComputed(computed);
	}
}

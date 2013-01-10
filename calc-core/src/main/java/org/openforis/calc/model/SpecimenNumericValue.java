package org.openforis.calc.model;


/**
 * @author G. Miceli
 */
public class SpecimenMeasurement extends org.openforis.calc.persistence.jooq.tables.pojos.SpecimenMeasurement implements Identifiable {

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

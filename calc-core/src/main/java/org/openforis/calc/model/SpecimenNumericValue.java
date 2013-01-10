package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class SpecimenNumericValue extends org.openforis.calc.persistence.jooq.tables.pojos.SpecimenNumericValue implements Identifiable {

	private static final long serialVersionUID = 1L;

	public SpecimenNumericValue() {
	}

	public SpecimenNumericValue(Specimen specimen, Variable var, Double value, boolean computed) {
		setSpecimenId(specimen.getId());
		setVariableId(var.getId());
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

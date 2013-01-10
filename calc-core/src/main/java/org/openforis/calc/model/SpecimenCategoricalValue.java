package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class SpecimenCategoricalValue extends org.openforis.calc.persistence.jooq.tables.pojos.SpecimenCategoricalValue implements Identifiable {

	private static final long serialVersionUID = 1L;

	public SpecimenCategoricalValue() {
	}

	public SpecimenCategoricalValue(Specimen specimen, Category cat, boolean computed) {
		setSpecimenId(specimen.getId());
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

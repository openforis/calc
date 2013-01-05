package org.openforis.calc.model;


/**
 * @author G. Miceli
 */
public class SpecimenCategory extends org.openforis.calc.persistence.jooq.tables.pojos.SpecimenCategory implements Identifiable {

	private static final long serialVersionUID = 1L;

	public SpecimenCategory() {
	}
	
	public SpecimenCategory(Specimen specimen, Category cat, boolean computed) {
		setSpecimenId(specimen.getId());
		setCategoryId(cat.getId());
		setComputed(computed);
	}
}

package org.openforis.calc.model;

import org.openforis.calc.persistence.jooq.tables.pojos.SpecimenCategoryBase;

/**
 * @author G. Miceli
 */
public class SpecimenCategory extends SpecimenCategoryBase implements ModelObject {

	private static final long serialVersionUID = 1L;

	public SpecimenCategory() {
	}
	
	public SpecimenCategory(Specimen specimen, Category cat, boolean computed) {
		setSpecimenId(specimen.getId());
		setCategoryId(cat.getId());
		setComputed(computed);
	}
}

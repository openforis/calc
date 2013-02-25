package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class Specimen extends org.openforis.calc.persistence.jooq.tables.pojos.Specimen implements Observation {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getSpecimenId();
	}

	@Override
	public void setId(Integer id) {
		super.setSpecimenId(id);
	}

}

package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class InterviewNumericValue extends org.openforis.calc.persistence.jooq.tables.pojos.InterviewNumericValue implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getValueId();
	}

	@Override
	public void setId(Integer id) {
		super.setValueId(id);
	}

}

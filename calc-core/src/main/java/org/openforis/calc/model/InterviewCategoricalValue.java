package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class InterviewCategoricalValue extends org.openforis.calc.persistence.jooq.tables.pojos.InterviewCategoricalValue implements Identifiable {

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

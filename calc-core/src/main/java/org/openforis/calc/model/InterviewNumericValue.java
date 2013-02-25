package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class InterviewNumericValue extends org.openforis.calc.persistence.jooq.tables.pojos.InterviewNumericValue implements NumericValue {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getValueId();
	}

	@Override
	public void setId(Integer id) {
		super.setValueId(id);
	}

	@Override
	public Integer getObservationId() {
		return getInterviewId();
	}

	@Override
	public void setObservationId(Integer obsId) {
		setInterviewId(obsId);
	}
}

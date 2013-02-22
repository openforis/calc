package org.openforis.calc.model;


/**
 * @author G. Miceli
 */
public class Interview extends org.openforis.calc.persistence.jooq.tables.pojos.Interview implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getInterviewId();
	}

	@Override
	public void setId(Integer id) {
		super.setInterviewId(id);
	}

	public void setInterviewDate(java.util.Date interviewDate) {
		super.setInterviewDate(new java.sql.Date(interviewDate.getTime()));
	}
}

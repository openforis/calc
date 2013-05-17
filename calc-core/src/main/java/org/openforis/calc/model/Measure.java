package org.openforis.calc.model;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class Measure extends org.openforis.calc.persistence.jooq.tables.pojos.Measure {

	private static final long serialVersionUID = 1L;

	private Survey survey;
	private org.openforis.calc.persistence.jooq.tables.pojos.Measure measure;

	public Measure(Survey survey, org.openforis.calc.persistence.jooq.tables.pojos.Measure measure) {
		this.survey = survey;
		this.measure = measure;
	}
	
	
	
}

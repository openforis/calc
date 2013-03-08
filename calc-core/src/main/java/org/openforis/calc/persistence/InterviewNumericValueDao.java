package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.impl.Factory;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.Interview;
import org.openforis.calc.persistence.jooq.tables.InterviewNumericValue;
import org.openforis.calc.persistence.jooq.tables.records.InterviewNumericValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class InterviewNumericValueDao extends JooqDaoSupport<InterviewNumericValueRecord, InterviewNumericValue> {

	public InterviewNumericValueDao() {
		super(INTERVIEW_NUMERIC_VALUE, InterviewNumericValue.class);
	}

	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		InterviewNumericValue v = INTERVIEW_NUMERIC_VALUE.as("v");
		Interview o = INTERVIEW.as("o");
		create.delete(v)
			  .where(v.INTERVIEW_ID.in(
					  	create.select(o.INTERVIEW_ID)
					  		  .from(o)
					  		  .where(o.OBS_UNIT_ID.eq(id))));
	}
}

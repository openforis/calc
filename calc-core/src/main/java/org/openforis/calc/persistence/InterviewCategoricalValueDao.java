package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.impl.Factory;
import org.openforis.calc.model.InterviewCategoricalValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.Interview;
import org.openforis.calc.persistence.jooq.tables.records.InterviewCategoricalValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class InterviewCategoricalValueDao extends JooqDaoSupport<InterviewCategoricalValueRecord, InterviewCategoricalValue> {

	public InterviewCategoricalValueDao() {
		super(INTERVIEW_CATEGORICAL_VALUE, InterviewCategoricalValue.class);
	}
	
	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.InterviewCategoricalValue v = INTERVIEW_CATEGORICAL_VALUE.as("v");
		Interview o = INTERVIEW.as("o");
		create.delete(v)
			  .where(v.INTERVIEW_ID.in(
					  	create.select(o.INTERVIEW_ID)
					  		  .from(o)
					  		  .where(o.OBS_UNIT_ID.eq(id))));
	}
}

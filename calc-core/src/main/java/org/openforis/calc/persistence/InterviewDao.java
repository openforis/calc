package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.impl.Factory;
import org.openforis.calc.model.Interview;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.records.InterviewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class InterviewDao extends JooqDaoSupport<InterviewRecord, Interview> {

	public InterviewDao() {
		super(INTERVIEW, Interview.class);
	}
	
	public int nextId() {
		Factory create = getJooqFactory();
		return create.nextval(Sequences.INTERVIEW_INTERVIEW_ID_SEQ).intValue();
	}

	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.Interview i = INTERVIEW.as("i");
		create.delete(i).where(i.OBS_UNIT_ID.eq(id));
	}
}

package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.INTERVIEW_NUMERIC_VALUE;

import org.openforis.calc.model.InterviewNumericValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
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
}

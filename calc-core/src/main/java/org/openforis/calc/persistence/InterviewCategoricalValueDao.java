package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.openforis.calc.model.InterviewCategoricalValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
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
}

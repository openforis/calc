package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.tables.Stratum.STRATUM;

import java.util.List;

import org.openforis.calc.model.Stratum;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.StratumRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class StratumDao extends JooqDaoSupport<StratumRecord, Stratum> {

	public StratumDao() {
		super(Tables.STRATUM, Stratum.class);
	}

	public List<Stratum> findBySurveyId(int surveyId) {
		return fetch(STRATUM.SURVEY_ID, surveyId);
	}
}
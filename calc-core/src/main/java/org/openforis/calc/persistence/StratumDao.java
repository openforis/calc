package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.tables.Stratum.*;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.model.Stratum;
import org.openforis.calc.model.IdentifierMap;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.StratumRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class StratumDao extends JooqDaoSupport<StratumRecord, Stratum> {

	public StratumDao() {
		super(Tables.STRATUM, Stratum.class);
	}

	public IdentifierMap loadIdentifiers(int surveyId) {
		IdentifierMap map = new IdentifierMap();
		Factory create = getJooqFactory();
		Result<Record> result = 
		    create.select(STRATUM.ID, STRATUM.NO, STRATUM.CODE)
		          .from(STRATUM)
		          .where(STRATUM.SURVEY_ID.eq(surveyId))
		          .fetch();
		
		for (Record record : result) {
			Integer stratumId = record.getValue(STRATUM.ID);
			Integer stratumNo = record.getValue(STRATUM.NO);
			String stratumCode = record.getValue(STRATUM.CODE);
			map.put(stratumId, stratumNo, stratumCode);
		}
		return map;
	}
}
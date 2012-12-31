package org.openforis.calc.persistence;

import org.openforis.calc.model.Stratum;
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

}
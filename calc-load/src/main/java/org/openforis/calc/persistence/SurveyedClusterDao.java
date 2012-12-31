package org.openforis.calc.persistence;

import org.openforis.calc.model.SurveyedCluster;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.SurveyedClusterRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class SurveyedClusterDao extends JooqDaoSupport<SurveyedClusterRecord, SurveyedCluster> {

	public SurveyedClusterDao() {
		super(Tables.SURVEYED_CLUSTER, SurveyedCluster.class);
	}
}
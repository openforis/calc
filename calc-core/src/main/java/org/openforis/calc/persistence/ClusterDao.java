package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.tables.Cluster.*;

import java.util.List;

import org.openforis.calc.model.Cluster;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.ClusterRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class ClusterDao extends JooqDaoSupport<ClusterRecord, Cluster> {

	public ClusterDao() {
		super(Tables.CLUSTER, Cluster.class);
	}

	public List<Cluster> findBySurveyId(int surveyId) {
		return fetch(CLUSTER.SURVEY_ID, surveyId);
	}
}
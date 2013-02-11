package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.tables.Cluster.CLUSTER;

import java.util.List;

import org.jooq.impl.Factory;
import org.openforis.calc.model.Cluster;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
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
	
	public int nextId() {
		Factory create = getJooqFactory();
		return create.nextval(Sequences.CLUSTER_ID_SEQ).intValue();
	}
}
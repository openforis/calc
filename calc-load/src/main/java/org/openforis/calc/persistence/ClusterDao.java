package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.tables.ClusterTable.*;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.model.Cluster;
import org.openforis.calc.model.IdentifierMap;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.ClusterRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class ClusterDao extends JooqDaoSupport<ClusterRecord, Cluster> {

	public ClusterDao() {
		super(Tables.CLUSTER, Cluster.class);
	}

	public IdentifierMap loadIdentifiers(int surveyId) {
		IdentifierMap map = new IdentifierMap();
		Factory create = getJooqFactory();
		Result<Record> result = 
		    create.select(CLUSTER.ID, CLUSTER.NO, CLUSTER.CODE)
		          .from(CLUSTER)
		          .where(CLUSTER.SURVEY_ID.eq(surveyId))
		          .fetch();
		
		for (Record record : result) {
			Integer id = record.getValue(CLUSTER.ID);
			Integer no = record.getValue(CLUSTER.NO);
			String code = record.getValue(CLUSTER.CODE);
			map.put(id, no, code);
		}
		return map;
	}
}
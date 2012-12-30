package org.openforis.calc.persistence;

import org.openforis.calc.model.Cluster;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.ClusterRecord;

/**
 * @author G. Miceli
 */
public class ClusterDao extends JooqDaoSupport<ClusterRecord, Cluster> {

	public ClusterDao() {
		super(Tables.CLUSTER, Cluster.class);
	}

}
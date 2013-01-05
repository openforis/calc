package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.tables.Plot.*;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.model.Plot;
import org.openforis.calc.model.PlotIdentifierMap;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.PlotRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class PlotDao extends JooqDaoSupport<PlotRecord, Plot> {

	public PlotDao() {
		super(Tables.PLOT, Plot.class);
	}

	public PlotIdentifierMap loadGroundPlotIdentifiers(int surveyId) {
		PlotIdentifierMap map = new PlotIdentifierMap();
		Factory create = getJooqFactory();
		Result<Record> result = 
		    create.select(PLOT.ID, PLOT.NO, PLOT.CODE, PLOT.CLUSTER_ID)
		          .from(PLOT)
		          .where(PLOT.SURVEY_ID.eq(surveyId).and(PLOT.GROUND_PLOT.isTrue()))
		          .fetch();
		
		for (Record record : result) {
			int clusterId = record.getValue(PLOT.CLUSTER_ID);
			int id = record.getValue(PLOT.ID);
			Integer no = record.getValue(PLOT.NO);
			String code = record.getValue(PLOT.CODE);
			map.put(clusterId, id, no, code);
		}
		return map;
	}
}
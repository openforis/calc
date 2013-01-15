package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT_VISITED_CNT_VIEW;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.SamplePlotVisitedCntView;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SamplePlotVisitedCntViewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@Component
@Transactional
public class SamplePlotVisitedCntViewDao extends JooqDaoSupport<SamplePlotVisitedCntViewRecord, SamplePlotVisitedCntView> {

	public SamplePlotVisitedCntViewDao() {
		super(SAMPLE_PLOT_VISITED_CNT_VIEW, SamplePlotVisitedCntView.class);
	}

	public FlatDataStream getCountsByObsUnit(int obsUnitId) {
		Factory create = getJooqFactory();
		
		Result<Record> result = 
				create.select()
				.from( SAMPLE_PLOT_VISITED_CNT_VIEW )
				.where( SAMPLE_PLOT_VISITED_CNT_VIEW.PLOT_OBS_UNIT_ID.eq(obsUnitId) )
				.fetch();
		
		return stream(result);
	}
	
}

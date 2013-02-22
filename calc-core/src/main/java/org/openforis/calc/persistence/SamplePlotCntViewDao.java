package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT_CNT_VIEW;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.SamplePlotCntView;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SamplePlotCntViewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@Component
@Transactional
public class SamplePlotCntViewDao extends JooqDaoSupport<SamplePlotCntViewRecord, SamplePlotCntView> {

	public SamplePlotCntViewDao() {
		super(SAMPLE_PLOT_CNT_VIEW, SamplePlotCntView.class);
	}

	@Deprecated
	public FlatDataStream getCountsByObsUnit(int obsUnitId) {
		Factory create = getJooqFactory();
		
//		Result<Record> result = 
//				create.select()
//				.from( SAMPLE_PLOT_CNT_VIEW )
//				.where( SAMPLE_PLOT_CNT_VIEW.PLOT_OBS_UNIT_ID.eq(obsUnitId) )
//				.fetch();
//		
//		return stream(result);
		return null;
	}
	
}

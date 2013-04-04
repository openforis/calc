package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT_VIEW;

import org.jooq.Field;
import org.openforis.calc.model.SamplePlotView;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SamplePlotViewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@Component
@Transactional
public class SamplePlotViewDao extends JooqDaoSupport<SamplePlotViewRecord, SamplePlotView> {

	private static final org.openforis.calc.persistence.jooq.tables.SamplePlotView V = SAMPLE_PLOT_VIEW;

	public SamplePlotViewDao() {
		super(V, SamplePlotView.class, V.PLOT_OBS_UNIT_ID, V.CLUSTER_CODE, V.PLOT_NO);
	}

	public Integer getId(int obsUnitId, String clusterCode, int plotNo) {
		return getIdByKey(obsUnitId, clusterCode, plotNo);
	}

	@Override
	protected Field<?> pk() {
		return V.SAMPLE_PLOT_ID;
	}

}

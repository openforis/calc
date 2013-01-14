package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_VIEW;

import org.jooq.Field;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.PlotSectionView;
import org.openforis.calc.persistence.jooq.tables.records.PlotSectionViewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author Mino Togna
 */
@Component
@Transactional
public class PlotSectionViewDao extends JooqDaoSupport<PlotSectionViewRecord, PlotSectionView> {

	private static final PlotSectionView V = PLOT_SECTION_VIEW;

	public PlotSectionViewDao() {
		super(V, PlotSectionView.class, V.PLOT_OBS_UNIT_ID, V.CLUSTER_CODE, V.PLOT_NO, V.PLOT_SECTION, V.VISIT_TYPE);
	}

	public Integer getId(int obsUnitId, String clusterCode, int plotNo, String plotSection, String visitType) {
		return getIdByKey(obsUnitId, clusterCode, plotNo, plotSection, visitType);
	}

	@Override
	protected Field<?> pk() {
		return V.PLOT_SECTION_ID;
	}

	public Object[] extractKey(FlatRecord r, int obsUnitId) {
		return extractKey(r, obsUnitId, V.CLUSTER_CODE, V.PLOT_NO, V.PLOT_SECTION, V.VISIT_TYPE);
	}

	public FlatDataStream streamAll(String[] fields, int observationUnitId) {
		return stream(fields, V.PLOT_OBS_UNIT_ID, observationUnitId);
	}
}

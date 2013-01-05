package org.openforis.calc.persistence;

import org.openforis.calc.model.PlotMeasurement;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.PlotMeasurementRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class PlotMeasurementDao extends JooqDaoSupport<PlotMeasurementRecord, PlotMeasurement> {

	public PlotMeasurementDao() {
		super(Tables.PLOT_MEASUREMENT, PlotMeasurement.class);
	}
}
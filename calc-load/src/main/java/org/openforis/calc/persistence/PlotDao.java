package org.openforis.calc.persistence;

import org.openforis.calc.model.Plot;
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

}
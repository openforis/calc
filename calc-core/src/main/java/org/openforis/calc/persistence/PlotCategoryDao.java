package org.openforis.calc.persistence;

import org.openforis.calc.model.PlotCategory;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.PlotCategoryRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class PlotCategoryDao extends JooqDaoSupport<PlotCategoryRecord, PlotCategory> {

	public PlotCategoryDao() {
		super(Tables.PLOT_CATEGORY, PlotCategory.class);
	}
}
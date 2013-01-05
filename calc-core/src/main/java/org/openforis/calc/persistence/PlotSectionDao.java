package org.openforis.calc.persistence;

import org.openforis.calc.model.PlotSection;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.PlotSectionRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class PlotSectionDao extends JooqDaoSupport<PlotSectionRecord, PlotSection> {

	public PlotSectionDao() {
		super(Tables.PLOT_SECTION, PlotSection.class);
	}
}
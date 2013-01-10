package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION;

import org.openforis.calc.model.PlotSection;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
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
		super(PLOT_SECTION, PlotSection.class, 
				PLOT_SECTION.SAMPLE_PLOT_ID, PLOT_SECTION.PLOT_SECTION_, PLOT_SECTION.VISIT_TYPE);
	}

	public Integer getId(int samplePlotId, String section, String visitType) {
		return getIdByKey(samplePlotId, section, visitType);
	}
}
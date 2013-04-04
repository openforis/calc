package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.impl.Factory;
import org.openforis.calc.model.PlotCategoricalValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.PlotSection;
import org.openforis.calc.persistence.jooq.tables.SamplePlot;
import org.openforis.calc.persistence.jooq.tables.records.PlotCategoricalValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class PlotCategoricalValueDao extends JooqDaoSupport<PlotCategoricalValueRecord, PlotCategoricalValue> {

	public PlotCategoricalValueDao() {
		super(Tables.PLOT_CATEGORICAL_VALUE, PlotCategoricalValue.class);
	}

	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.PlotCategoricalValue v = PLOT_CATEGORICAL_VALUE;
		PlotSection ps = PLOT_SECTION;
		SamplePlot sp = SAMPLE_PLOT;
		create.delete(v)
			  .where(v.PLOT_SECTION_ID.in(
					  	create.select(ps.PLOT_SECTION_ID)
					  		  .from(ps)
					  		  .join(sp).on(ps.SAMPLE_PLOT_ID.eq(sp.SAMPLE_PLOT_ID))
					  		  .where(sp.OBS_UNIT_ID.eq(id)))).execute();
	}
}

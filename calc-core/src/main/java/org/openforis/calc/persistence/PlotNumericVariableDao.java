package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.impl.Factory;
import org.openforis.calc.model.PlotNumericValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.PlotSection;
import org.openforis.calc.persistence.jooq.tables.SamplePlot;
import org.openforis.calc.persistence.jooq.tables.records.PlotNumericValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class PlotNumericVariableDao extends JooqDaoSupport<PlotNumericValueRecord, PlotNumericValue> {

	public PlotNumericVariableDao() {
		super(Tables.PLOT_NUMERIC_VALUE, PlotNumericValue.class);
	}

	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.PlotNumericValue v = PLOT_NUMERIC_VALUE;
		PlotSection ps = PLOT_SECTION.as("o");
		SamplePlot sp = SAMPLE_PLOT.as("sp");
		
		create.delete(v)
			  .where(v.PLOT_SECTION_ID.in(
					  	create.select(ps.PLOT_SECTION_ID)
					  		  .from(ps)
					  		  .join(sp).on(sp.SAMPLE_PLOT_ID.eq(ps.SAMPLE_PLOT_ID))
					  		  .where(sp.OBS_UNIT_ID.eq(id)))).execute();
	}

}
package org.openforis.calc.persistence;

import org.openforis.calc.model.PlotNumericValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
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
	
}
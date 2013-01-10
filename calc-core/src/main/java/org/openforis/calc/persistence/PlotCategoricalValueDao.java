package org.openforis.calc.persistence;

import org.openforis.calc.model.PlotCategoricalValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
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
}
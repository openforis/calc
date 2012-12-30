package org.openforis.calc.persistence;

import org.openforis.calc.model.CategoricalVariable;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.CategoricalVariableRecord;

/**
 * @author G. Miceli
 */
public class CategoricalVariableDao extends JooqDaoSupport<CategoricalVariableRecord, CategoricalVariable> {

	public CategoricalVariableDao() {
		super(Tables.CATEGORICAL_VARIABLE, CategoricalVariable.class);
	}
}

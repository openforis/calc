package org.openforis.calc.persistence;

import org.openforis.calc.model.NumericVariable;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.NumericVariableRecord;

/**
 * @author G. Miceli
 */
public class NumericVariableDao extends JooqDaoSupport<NumericVariableRecord, NumericVariable> {

	public NumericVariableDao() {
		super(Tables.NUMERIC_VARIABLE, NumericVariable.class);
	}
}

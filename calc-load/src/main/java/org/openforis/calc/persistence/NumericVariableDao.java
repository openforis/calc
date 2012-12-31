package org.openforis.calc.persistence;

import java.util.List;

import org.openforis.calc.model.NumericVariable;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.NumericVariableRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class NumericVariableDao extends JooqDaoSupport<NumericVariableRecord, NumericVariable> {

	public NumericVariableDao() {
		super(Tables.NUMERIC_VARIABLE, NumericVariable.class);
	}
	
	public List<NumericVariable> fetchByObservationUnitId(int surveyUnitId) {
		return fetch(Tables.NUMERIC_VARIABLE.OBS_UNIT_ID, surveyUnitId);
	}

	public List<NumericVariable> fetchByObservationUnit(ObservationUnit level) {
		return fetchByObservationUnitId(level.getId());
	}
}

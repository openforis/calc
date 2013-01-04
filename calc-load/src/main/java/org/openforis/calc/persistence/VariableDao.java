package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import java.util.List;

import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.VariableRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class VariableDao extends JooqDaoSupport<VariableRecord, Variable> {

	public VariableDao() {
		super(VARIABLE, Variable.class);
	}
	
	public List<Variable> fetchByObservationUnitId(int surveyUnitId) {
		return fetch(VARIABLE.OBS_UNIT_ID, surveyUnitId);
	}

	public List<Variable> fetchByObservationUnit(ObservationUnit level) {
		return fetchByObservationUnitId(level.getId());
	}
}

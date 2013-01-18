package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.VARIABLE;

import java.util.List;

import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.VariableRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class VariableDao extends JooqDaoSupport<VariableRecord, Variable> {

	public VariableDao() {
		super(VARIABLE, Variable.class);
	}
	
	public List<Variable> findByObservationUnitId(int id) {
		return fetch(VARIABLE.OBS_UNIT_ID, id);
	}
}

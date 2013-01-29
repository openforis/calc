package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.VARIABLE;

import java.util.List;

import org.jooq.impl.Factory;
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
	
	public Variable findByName(String name, int observationUnitIt){
		Factory create = getJooqFactory();
		
		VariableRecord record = create			
			.selectFrom(VARIABLE)
			.where(
					VARIABLE.VARIABLE_NAME.eq (name )
					.and( VARIABLE.OBS_UNIT_ID.eq(observationUnitIt) )
			).fetchOne();
		
		return record.into( Variable.class );
	}

//	private Variable fromRecord(VariableRecord record) {
//		Variable variable = new Variable();
//		variable.setObsUnitId( record.getObsUnitId() );
//		variable.setVariableDescription( record.getVariableDescription() );
//		variable.setVariableId( record.getVariableId() );
//		variable.setVariableLabel( record.getVariableLabel() );
//		variable.setVariableName( record.getVariableName() );
//		variable.setVariableOrder( record.getVariableOrder() );
//		variable.setVariableType( record.getVariableType() );
//		return variable;
//	}
}

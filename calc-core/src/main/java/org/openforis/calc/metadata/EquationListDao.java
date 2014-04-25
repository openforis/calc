/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.List;

import org.jooq.Configuration;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Mino Togna
 *
 */
public class EquationListDao extends org.openforis.calc.persistence.jooq.tables.daos.EquationListDao {

	@Autowired
	private Psql psql;
	
	public EquationListDao(Configuration configuration) {
		super(configuration);
	}

	@Transactional
	public void loadByWorkspace( Workspace workspace ) {
		
		List<EquationList> list = super.fetchByWorkspaceId( workspace.getId() );
		
		for (EquationList eqList : list) {
			// load equations
			List<Equation> equations = psql
				.select()
				.from( Tables.EQUATION )
				.where( Tables.EQUATION.LIST_ID.eq(eqList.getId()) )
				.fetchInto( Equation.class );
			
			eqList.setEquations( equations );
			
		}
		// add equation list to workspace
		workspace.setEquationLists( list );
		
	}
	
	
}

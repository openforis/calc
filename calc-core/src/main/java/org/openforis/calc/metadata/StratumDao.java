package org.openforis.calc.metadata;

import java.util.List;

import org.jooq.Configuration;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
public class StratumDao extends org.openforis.calc.persistence.jooq.tables.daos.StratumDao {
	
	@Autowired
	private Psql psql;
	
	public StratumDao() {
	}
	
	public StratumDao(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Delete all strata of the given workspace
	 * @param workspace
	 */
	@Transactional
	public void deleteAll(Workspace workspace) {
		
		List<Stratum> strata = workspace.getStrata();
		delete(strata);
		
		workspace.emptyStrata();
	}
	
	/**
	 * Creates a new stratum with given stratumNo and caption and it gets associated with the workspace
	 * @param workspace
	 * @param stratumNo
	 * @param caption
	 */
	@Transactional
	public void insert( Workspace workspace, int stratumNo, String caption ) {
		Stratum stratum = new Stratum();
		
		Long nextval = psql.nextval( Sequences.STRATUM_ID_SEQ );
		stratum.setId( nextval.intValue() );
		
		stratum.setStratumNo(stratumNo);
		stratum.setCaption(caption);
		
		workspace.addStratum(stratum);
		
		insert(stratum);
	}
	
}

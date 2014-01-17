package org.openforis.calc.metadata;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
@Component
public class StratumDao extends AbstractJpaDao<Stratum> {

	/**
	 * Delete all strata of the given workspace
	 * @param workspace
	 */
	@Transactional
	public void deleteAll(Workspace workspace){
		String sql = "delete Stratum where workspace.id = :wsId";

		getEntityManager()
		.createQuery(sql)
		.setParameter( "wsId", workspace.getId() )
		.executeUpdate();
		
		workspace.emptyStrata();
	}
	
	/**
	 * Creates a new stratum with given stratumNo and caption and it gets associated with the workspace
	 * @param workspace
	 * @param stratumNo
	 * @param caption
	 */
	@Transactional
	public void add(Workspace workspace, int stratumNo, String caption){
		Stratum stratum = new Stratum();
		stratum.setStratumNo(stratumNo);
		stratum.setCaption(caption);
		stratum.setWorkspace(workspace);
		
		stratum = this.create(stratum);
		workspace.addStratum(stratum);
	}
	
}

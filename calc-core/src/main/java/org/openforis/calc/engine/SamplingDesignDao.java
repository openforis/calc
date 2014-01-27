package org.openforis.calc.engine;

import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 * 
 */
@Repository
public class SamplingDesignDao extends AbstractJpaDao<SamplingDesign> {

	@Transactional
	public void deleteByWorkspace(int workspaceId) {
		String sql = "delete SamplingDesign where workspace.id = :wsId";

		getEntityManager()
		.createQuery(sql)
		.setParameter("wsId", workspaceId)
		.executeUpdate();

	}

}

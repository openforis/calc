package org.openforis.calc.engine;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
@Repository
public class WorkspaceDao extends AbstractJpaDao<Workspace> {

	@Transactional
	public Workspace fetchByName(String name) {
		return fetchFirst("name", name);
	}

	@Transactional
	public Workspace fetchByCollectSurveyUri(String uri) {
		return fetchFirst("collectSurveyUri", uri);
	}

	@Transactional
	public Workspace fetchActive() {
		return fetchFirst("active", true);
	}

	@Transactional
	public void deactivateAll() {
		EntityManager em = getEntityManager();
		Query query = em.createQuery("update Workspace set active = false");
		query.executeUpdate();
		em.flush();
	}

}

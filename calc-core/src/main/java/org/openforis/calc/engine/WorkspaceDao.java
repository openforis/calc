package org.openforis.calc.engine;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 */
@Repository
public class WorkspaceDao extends AbstractJpaDao<Workspace> {

	@Transactional
	public Workspace fetchByName(String name) {
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Workspace> q = cb.createQuery(Workspace.class);
		Root<Workspace> root = q.from(Workspace.class);
		q.where(cb.equal(root.get("name"), name));
		List<Workspace> results = em.createQuery(q).getResultList();
		if ( results != null && ! results.isEmpty() ) {
			return results.get(0);
		} else {
			return null;
		}
	}
	
	@Transactional
	public Workspace fetchByCollectSurveyUri(String uri) {
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Workspace> q = cb.createQuery(Workspace.class);
		Root<Workspace> root = q.from(Workspace.class);
		q.where(cb.equal(root.get("collectSurveyUri"), uri));
		List<Workspace> results = em.createQuery(q).getResultList();
		if ( results != null && ! results.isEmpty() ) {
			return results.get(0);
		} else {
			return null;
		}
	}
	

}

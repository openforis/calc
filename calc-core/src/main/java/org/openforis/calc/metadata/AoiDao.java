package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
@Component
public class AoiDao extends AbstractJpaDao<Aoi> {

	@Transactional
	public void deleteByLevels(Collection<AoiLevel> levels) {
		List<Integer> levelIds = new ArrayList<Integer>(levels.size());
		for (AoiLevel level : levels) {
			levelIds.add( level.getId() );
		}

		String sql = "delete Aoi where aoiLevel.id IN :levels";

		getEntityManager()
			.createQuery(sql)
			.setParameter("levels", levelIds )
			.executeUpdate();
	}

	public void assignRootAoi(AoiHierarchy aoiHierarchy) {
		Set<AoiLevel> levels = aoiHierarchy.getLevels();
		if(!levels.isEmpty()){
			AoiLevel rootLevel = levels.iterator().next();
			
			EntityManager em = getEntityManager();			
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aoi> cq = cb.createQuery(Aoi.class);

			Root<Aoi> root = cq.from(Aoi.class);
			cq.multiselect( root.get("id"), root.get("code"), root.get("caption"), root.get("landArea") );
			cq.where( cb.equal(root.get("aoiLevel").get("id"), rootLevel.getId()) );
			
			Aoi aoi = em.createQuery(cq).getResultList().get(0);
			loadChildren(aoi, 0);
			aoiHierarchy.setRootAoi(aoi);
		}

	}

	// load children aoi up to level 2
	private void loadChildren(Aoi aoi, int depth) {
		if( depth <= 1 ) {
			EntityManager em = getEntityManager();			
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aoi> cq = cb.createQuery(Aoi.class);

			Root<Aoi> root = cq.from(Aoi.class);
			cq.multiselect( root.get("id"), root.get("code"), root.get("caption"), root.get("landArea") );
			cq.where( cb.equal(root.get("parentAoi").get("id"), aoi.getId()) );
			
			List<Aoi> children = em.createQuery(cq).getResultList();
			aoi.setChildren( children );
			for (Aoi child : children) {
				loadChildren(child, depth+1);
			}
		}
	}
	
}

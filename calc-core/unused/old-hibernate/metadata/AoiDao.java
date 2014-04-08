package org.openforis.calc.metadata;

import java.util.Collection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
@Component
public class AoiDao extends org.openforis.calc.persistence.jooq.tables.daos.AoiDao {

	@Transactional
	public void deleteByLevels(Collection<AoiLevel> levels) {
		for (AoiLevel aoiLevel : levels) {
			super.delete( aoiLevel.getAois() );
		}
	}

//	public void assignRootAoi(AoiHierarchy aoiHierarchy) {
//		Set<AoiLevel> levels = aoiHierarchy.getLevels();
//		if(!levels.isEmpty()){
//			AoiLevel rootLevel = levels.iterator().next();
//			
//			EntityManager em = getEntityManager();			
//			CriteriaBuilder cb = em.getCriteriaBuilder();
//			CriteriaQuery<Aoi> cq = cb.createQuery(Aoi.class);
//
//			Root<Aoi> root = cq.from(Aoi.class);
//			cq.multiselect( root.get("id"), root.get("code"), root.get("caption"), root.get("landArea") );
//			cq.where( cb.equal(root.get("aoiLevel").get("id"), rootLevel.getId()) );
//			
//			Aoi aoi = em.createQuery(cq).getResultList().get(0);
//			loadChildren(aoi, 0);
//			aoiHierarchy.setRootAoi(aoi);
//		}

//	}

	// load children aoi up to level 2
//	private void loadChildren(Aoi aoi, int depth) {
//		if( depth <= 1 ) {
//			EntityManager em = getEntityManager();			
//			CriteriaBuilder cb = em.getCriteriaBuilder();
//			CriteriaQuery<Aoi> cq = cb.createQuery(Aoi.class);
//
//			Root<Aoi> root = cq.from(Aoi.class);
//			cq.multiselect( root.get("id"), root.get("code"), root.get("caption"), root.get("landArea") );
//			cq.where( cb.equal(root.get("parentAoi").get("id"), aoi.getId()) );
//			
//			List<Aoi> children = em.createQuery(cq).getResultList();
//			aoi.setChildren( children );
//			for (Aoi child : children) {
//				loadChildren(child, depth+1);
//			}
//		}
//	}
	
}

package org.openforis.calc.metadata;

import java.util.Collection;

import org.jooq.Configuration;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.AoiTable;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
public class AoiDao extends org.openforis.calc.persistence.jooq.tables.daos.AoiDao {

	@Autowired
	private Psql psql;
	
	public AoiDao() {
	}
	
	public AoiDao(Configuration configuration) {
		super(configuration);
	}

	@Transactional
	public void deleteByHierarchy( AoiHierarchy aoiHierarchy ) {
		
		Collection<AoiLevel> levelsReverseOrder = aoiHierarchy.getLevelsReverseOrder();
		AoiTable T = Tables.AOI;
		
		for (AoiLevel aoiLevel : levelsReverseOrder) {
			psql
				.delete( T )
				.where( T.AOI_LEVEL_ID.eq(aoiLevel.getId()) )
				.execute();
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

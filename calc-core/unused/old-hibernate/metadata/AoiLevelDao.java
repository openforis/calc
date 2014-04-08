package org.openforis.calc.metadata;

import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
public class AoiLevelDao extends org.openforis.calc.persistence.jooq.tables.daos.AoiLevelDao {

	@Transactional
	public void deleteByHierarchy( AoiHierarchy hierarchy ) {
		
//		super
		
//		String sql = "delete AoiLevel where hierarchy.id = :hierarchyId";

//		getEntityManager().createQuery(sql).setParameter("hierarchyId", hierarchy.getId()).executeUpdate();
	}
}

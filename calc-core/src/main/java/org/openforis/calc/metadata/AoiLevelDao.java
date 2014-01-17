package org.openforis.calc.metadata;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Mino Togna
 * 
 */
@Component
public class AoiLevelDao extends AbstractJpaDao<AoiLevel> {

	@Transactional
	public void deleteByHierarchy(AoiHierarchy hierarchy) {
		String sql = "delete AoiLevel where hierarchy.id = :hierarchyId";

		getEntityManager().createQuery(sql).setParameter("hierarchyId", hierarchy.getId()).executeUpdate();
	}
}

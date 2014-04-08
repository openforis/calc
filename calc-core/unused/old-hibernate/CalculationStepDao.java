package org.openforis.calc.chain;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 * @author Mino Togna
 */
@Repository
public class CalculationStepDao extends AbstractJpaDao<CalculationStep> {

	@Transactional
	public int countOutputVariableSteps(int variableId) {
		Query query = getEntityManager().createQuery( "select count(*) from CalculationStep where outputVariable.id = " + variableId );
		Object result = query.getSingleResult();
		int count = ( (Long) result).intValue();
		return count;
	}

	@Transactional
	public void decrementFollowingStepNumbers(Integer chainId, int stepNo) {
		String query = String.format("update CalculationStep set stepNo = stepNo - 1 where processingChain.id = %d and stepNo > %d", chainId, stepNo);
		getEntityManager().createQuery(query).executeUpdate();
	}
	
	@Transactional
	public void incrementFollowingStepNumbers(Integer chainId, int stepNo) {
		String query = String.format("update CalculationStep set stepNo = stepNo + 1 where processingChain.id = %d and stepNo >= %d", chainId, stepNo);
		getEntityManager().createQuery(query).executeUpdate();
	}
	
	@Transactional
	public List<CalculationStep> findByProcessingChain( int chainId ) {
		String select = "select a from CalculationStep a where processingChain.id = :chainId order by stepNo" ;
		TypedQuery<CalculationStep> q = getEntityManager().createQuery( select, CalculationStep.class );
		q.setParameter( "chainId", chainId );
//		q.setHint("org.hibernate.cacheable", true);

		List<CalculationStep> list = q.getResultList();
		return list;
	}

}

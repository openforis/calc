package org.openforis.calc.chain;

import javax.persistence.Query;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
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
	
}

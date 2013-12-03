package org.openforis.calc.chain;

import javax.persistence.Query;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@Repository
public class CalculationStepDao extends AbstractJpaDao<CalculationStep> {

	@Transactional
	public boolean isVariableUsedInMultipleCalculationSteps(int variableId) {
		Query query = getEntityManager().createQuery("select count(*) from CalculationStep where outputVariable.id = " + variableId);
		Object result = query.getSingleResult();
		int count = ( (Long) result).intValue();
		return count > 1;
	}
	
}

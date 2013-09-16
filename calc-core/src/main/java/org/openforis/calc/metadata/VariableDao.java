/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.Collection;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.stereotype.Repository;

/**
 * @author S. Ricci
 *
 */
@Repository
public class VariableDao extends AbstractJpaDao<Variable> {
	
	public Collection<Variable<?>> deleteNotOverriddenVariables(Entity entity) {
		Collection<Variable<?>> notOverriddenVariables = entity.getNotOverriddenVariables();
		for (Variable<?> variable : notOverriddenVariables) {
			delete(variable.getId());
		}
		entity.removeVariables(notOverriddenVariables);
		return notOverriddenVariables;
	}

}

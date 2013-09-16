package org.openforis.calc.metadata;

import java.util.Collection;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
@Repository
public class EntityDao extends AbstractJpaDao<Entity> {
	
	@Autowired
	private VariableDao variableDao;

	@Transactional
	public Collection<Entity> removeNotOverriddenEntities(Workspace ws) {
		Collection<Entity> notOverriddenEntities = ws.removeNotOverriddenEntities();
		for (Entity entity : notOverriddenEntities) {
			List<Variable<?>> variables = entity.getVariables();
			for (Variable<?> variable : variables) {
				variableDao.delete(variable.getId());
			}
			delete(entity.getId());
		}
		return notOverriddenEntities;
	}
	
	
}

package org.openforis.calc.metadata;

import static org.openforis.calc.persistence.jooq.tables.EntityTable.ENTITY;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.AbstractManager;
import org.springframework.stereotype.Component;

/**
 * Manages persistence and creation of {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Component
public final class EntityManager extends AbstractManager<Entity>{
	
	public EntityManager() {
		super(Entity.class);
	}
	
	public List<Entity> getEntities(int workspaceId) {
		return createJooqDao().fetch(ENTITY.WORKSPACE_ID, workspaceId);
	}
}
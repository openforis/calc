package org.openforis.calc.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.EntityTable;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Entity Manger 
 * @author Mino Togna
 */

@Repository
public class EntityManager {

	@Autowired
	private EntityDao entityDao;
	
	@Autowired
	private VariableManager variableManager;
	
	@Autowired
	private Psql psql;
	
	public EntityManager(){
	}
	
	@Transactional
	public void loadEntities( Workspace workspace ) {
		List<Entity> entities = entityDao.fetchByWorkspaceId( workspace.getId() );
		Collections.sort( entities, new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				return o1.getSortOrder().compareTo( o2.getSortOrder() );
			}
		});
		for (Entity entity : entities) {
			workspace.addEntity( entity );
		}
		variableManager.loadByWorkspace( workspace );
	}
	
	@Transactional
	public void persistEntities(Workspace workspace) {
		Collection<Entity> rootEntities = workspace.getRootEntities();
		for (Entity entity : rootEntities) {
			entity.traverse(new Entity.Visitor() {
				@Override
				public void visit(Entity entity) {
					// set parentEntityId
					Entity parent = entity.getParent();
					if ( parent != null && entity.getParentEntityId() == null ) {
						entity.setParentEntityId( parent.getId() );
					}
					// insert or update entity
					Integer id = entity.getId();
					if( id == null ) {
						entity.setId( psql.nextval( Sequences.ENTITY_ID_SEQ ).intValue() );
						entityDao.insert( entity );
					} else {
						entityDao.update( entity );
					}
					// save variables
					List<Variable<?>> varList = entity.getVariables();
					Variable<?>[] variables = varList.toArray( new Variable<?>[varList.size()] );
					variableManager.save( variables );
				}
			});
		}
	}
	
	@Transactional
	public void delete( Entity entity ){
		Workspace ws = entity.getWorkspace();
		
		for ( Variable<?> variable : entity.getVariables() ) {
			variableManager.delete( variable );
		}
		
		entityDao.delete( entity );
		
		ws.removeEntity( entity );
	}

	@Transactional
	public void resetPlotAreaScript( Workspace workspace ) {
		EntityTable T = Tables.ENTITY;
		
		psql
			.update( T )
			.set( T.PLOT_AREA_SCRIPT, (String) null )
			.where( T.WORKSPACE_ID.eq(workspace.getId()) )
			.execute();
		
	}
	
	@Transactional
	public void importBackup( Workspace workspace , WorkspaceBackup workspaceBackup ) {

		List<Entity> entities = workspaceBackup.getWorkspace().getEntities();
		for ( Entity entity : entities ) {
			
			String plotAreaScript = entity.getPlotAreaScript();
			
			if( StringUtils.isNotBlank(plotAreaScript) ){
				Entity originalEntity = workspace.getEntityByOriginalId( entity.getOriginalId() );
				if( originalEntity == null ){
					throw new IllegalStateException( "Entity " + entity.getName() + " not found in workspace" );
				}
				originalEntity.setPlotAreaScript(plotAreaScript);
				entityDao.update( originalEntity );
			}
		}
	}
}

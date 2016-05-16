package org.openforis.calc.metadata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.openforis.calc.collect.SpeciesCodeTable;
import org.openforis.calc.collect.SpeciesCodeView;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.metadata.Variable.Type;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.EntityTable;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.EntityDataViewDao;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;
import org.openforis.calc.schema.TableDao;
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
	private EntityDataViewDao entityViewDao;

	@Autowired
	private TableDao tableDao;
	
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
	
	public void createOrUpdateView( Entity entity ){
		Workspace ws = entity.getWorkspace();

		Schemas schemas = new Schemas(ws);
		DataSchema inputSchema = schemas.getDataSchema();
		EntityDataView view = inputSchema.getDataView(entity);
		
		// drop view
		entityViewDao.drop(view);

		// create view
		Select<?> select = getViewSelect(entity);
		entityViewDao.create( view , select  );
	}
	
//	public void createOrUpdateView( Entity entity ) {
//		createOrUpdateView( entity , true );
//	}
	
	public Select<?> getViewSelect(Entity entity) {
		return getViewSelect( entity , false );
	}
	
	@SuppressWarnings("unchecked")
	public Select<?> getViewSelect(Entity entity, boolean addAlwaysWeightField) {
		Set<String> uniqueFields	= new HashSet<String>();
		Schemas schemas 			= entity.getWorkspace().schemas();
		DataSchema schema 			= schemas.getDataSchema();
		
		DataTable table 			= schema.getDataTable(entity);
		
		SelectQuery<Record> select 	= new Psql().selectQuery();
		select.addFrom(table);
		
		select.addSelect( table.getIdField() );
		select.addSelect( table.getTextFields() );
		select.addSelect( table.getAoiIdFields() );
		
		for (QuantitativeVariable var : entity.getOriginalQuantitativeVariables()) {
			Field<BigDecimal> field = table.getQuantityField(var);
			
			if( !uniqueFields.contains( field.getName() ) ){
				uniqueFields.add( field.getName() );
				
				select.addSelect( field );
			}
		}
		for (CategoricalVariable<?> var : entity.getOriginalCategoricalVariables()) {
			
			Field<Integer> categoryIdField = table.getCategoryIdField(var);
			if( !(categoryIdField==null || uniqueFields.contains(categoryIdField.getName())) ){
				uniqueFields.add( categoryIdField.getName() );
				
				select.addSelect( categoryIdField );
			}
			
			Field<?> categoryValueField = table.getCategoryValueField(var);
			if( !(categoryValueField==null || uniqueFields.contains(categoryValueField.getName())) ){
				uniqueFields.add( categoryValueField.getName() );
				
				if( var.getType() == Type.BINARY ){
					select.addSelect( DSL.coalesce(categoryValueField, "null").as(categoryValueField.getName()) );
				} else {
					select.addSelect( categoryValueField );	
				}
				
			}
		}
		
		for (MultiwayVariable variable : entity.getSpeciesCategoricalVariables()) {
			CategoryLevel categoryLevel 	= variable.getCategoryLevel();
			SpeciesCodeTable speciesTable 	= new SpeciesCodeTable( categoryLevel.getName(), categoryLevel.getSchemaName() );
			SpeciesCodeView speciesView 	= new SpeciesCodeView( speciesTable );
			
			
			select.addJoin(
					speciesView, 
					JoinType.LEFT_OUTER_JOIN, 
					speciesView.getCodeField().eq( (Field<String>) table.getCategoryValueField(variable) )
					);
			
			select.addSelect( DSL.coalesce( speciesView.getIdField() , -1 ).as(speciesView.getIdField().getName()) );
			select.addSelect( DSL.coalesce( speciesView.getGenusIdField() , -1 ).as(speciesView.getGenusIdField().getName()) );
		}
		
		// add weight column if sampling unit
		if(  entity.isSamplingUnit() ) {
			if( addAlwaysWeightField || tableDao.hasColumn( table, table.getWeightField() ) ){
				select.addSelect( table.getWeightField() );
			}
		}
		
		// for every ancestor, add join condition and select fields
		Entity currentEntity = entity;
		while ( currentEntity.getParent() != null ){
			DataTable currentTable 	= schema.getDataTable(currentEntity);
			Entity parentEntity 	= currentEntity.getParent();
			DataTable parentTable 	= schema.getDataTable( parentEntity );

			select.addSelect( parentTable.getIdField() );
			
			for (Field<?> field : parentTable.getCategoryValueFields()) {
				if( !(field==null || uniqueFields.contains(field.getName())) ){
					uniqueFields.add( field.getName() );
					select.addSelect( field );
				}
			}
			for (Field<?> field : parentTable.getCategoryIdFields()) {
				if( !(field==null || uniqueFields.contains(field.getName())) ){
					uniqueFields.add( field.getName() );
					select.addSelect( field );
				}
			}
			
//			select.addSelect( parentTable.getCategoryValueFields() );
//			select.addSelect( parentTable.getCategoryIdFields() );
			select.addSelect( parentTable.getTextFields() );
			if(  parentEntity.isSamplingUnit() ){
				if( addAlwaysWeightField || tableDao.hasColumn(table, table.getWeightField()) ){
					select.addSelect( parentTable.getWeightField() );
				}
			}
			for ( QuantitativeVariable var : parentEntity.getOriginalQuantitativeVariables() ) {
				select.addSelect( parentTable.getQuantityField(var) );
			}
//			if( parentTable.getWeightField() != null ){
//				select.addSelect( parentTable.getWeightField() );
//			}
			select.addJoin( parentTable, currentTable.getParentIdField().eq(parentTable.getIdField()) );
			
			if( parentEntity.isSamplingUnit() ){
//				if( parentEntity.isSamplingUnit() && joinWithResults ){
				addJoinWithResultsTable(parentEntity, parentTable, schema, select);
			}
//			addUniqueNameFields( select, parentTable.fields() );
			currentEntity = parentEntity;
		}
		
//		if( joinWithResults ){
			// add join with results table if exits
			addJoinWithResultsTable(entity, table, schema, select);
//		}
		
		return select;
	}

	private void addJoinWithResultsTable(Entity entity, DataTable table, DataSchema schema, SelectQuery<Record> select) {
		ResultTable resultTable = schema.getResultTable(entity);
		if( resultTable != null ){
			// select output variables from results table
			for (QuantitativeVariable var : entity.getDefaultProcessingChainQuantitativeOutputVariables()) {
				select.addSelect( resultTable.getQuantityField(var) );
			}
			for ( MultiwayVariable var : entity.getDefaultProcessingChainCategoricalOutputVariables() ){
				select.addSelect( resultTable.getCategoryIdField(var) );
				select.addSelect( resultTable.getCategoryValueField(var) );
			}
			if( resultTable.getPlotArea() != null ) {
				select.addSelect( resultTable.getPlotArea() );
			}
			
			select.addJoin(resultTable, JoinType.LEFT_OUTER_JOIN, resultTable.getIdField().eq(table.getIdField()));
		}
	}
}

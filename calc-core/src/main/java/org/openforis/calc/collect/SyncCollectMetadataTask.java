/**
 * 
 */
package org.openforis.calc.collect;

import static org.openforis.calc.persistence.jooq.tables.EntityTable.ENTITY;
import static org.openforis.calc.persistence.jooq.tables.VariableTable.VARIABLE;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jooq.impl.DSL;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceDao;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class SyncCollectMetadataTask extends Task {

	private static final String SPECIES_CODE_VAR_NAME = "species_code";
	private static final String SPECIES_SCIENT_NAME_VAR_NAME = "species_scient_name";
	private static final String DIMENSION_TABLE_SUFFIX = "_dim";

	private String surveyName;
	
	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Autowired
	private CollectSurveyIdmlBinder collectSurveyIdmlBinder;

	//transient variables
	private Map<Integer, Entity> entitiesByEntityDefinitionId;
	private Set<String> variableNames;
	private Set<String> outputValueColumnNames;

	@Override
	protected void execute() throws Throwable {
		entitiesByEntityDefinitionId = new HashMap<Integer, Entity>();
		variableNames = new HashSet<String>();
		outputValueColumnNames = new HashSet<String>();

		deleteNotOverriddenEntities();
		
		Workspace ws = getWorkspace();
		
		List<Entity> newEntities = createEntitiesFromSchema();
		
		List<Entity> entitiesToStore = mergeOldEntitiesWithNewOnes(newEntities);
		
		ws.setEntities(entitiesToStore);
		
		printToLog(entitiesToStore);
		
		workspaceDao.save(ws);
		
		//TODO children entity ids not updated after save...check this
		Workspace reloaded = workspaceDao.find(ws.getId());
		ws.setEntities(reloaded.getEntities());
	}

	private List<Entity> mergeOldEntitiesWithNewOnes(List<Entity> newEntities) {
		Workspace ws = getWorkspace();
		List<Entity> entitiesToStore = new ArrayList<Entity>();
		for (Entity newEntity : newEntities) {
			Entity entityToStore;
			Entity oldEntity = ws.getEntityByOriginalId(newEntity.getOriginalId());
			if ( oldEntity == null ) {
				entityToStore = newEntity;
			} else {
				merge(oldEntity, newEntity);
				entityToStore = oldEntity;
			}
			entityToStore.setSortOrder(entitiesToStore.size() + 1);
			entitiesToStore.add(entityToStore);
		}
		return entitiesToStore;
	}
	
	private List<Entity> createEntitiesFromSchema() throws IdmlParseException {
		Survey survey = loadSurvey();
		final RelationalSchema relationalSchema = generateSchema(survey);
		
		Schema schema = survey.getSchema();
		
		schema.traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition definition) {
				if ( definition.isMultiple() ) {
					Entity entity = createEntity(definition, relationalSchema);
					entity.setSortOrder(entitiesByEntityDefinitionId.size() + 1);
					entitiesByEntityDefinitionId.put(definition.getId(), entity);
				}
			}
		});
		return new ArrayList<Entity>(entitiesByEntityDefinitionId.values());
	}

	private Survey loadSurvey() throws IdmlParseException {
		// TODO get survey from Collect
		//return surveyDao.load( surveyName );
		InputStream surveyIs = getClass().getClassLoader().getResourceAsStream("test.idm.xml");
		Survey survey = collectSurveyIdmlBinder.unmarshal(surveyIs);
		return survey;
	}

	private Entity createEntity(NodeDefinition nodeDefinition, RelationalSchema relationalSchema) {
		Entity entity = new Entity();
		DataTable dataTable = relationalSchema.getDataTable(nodeDefinition);
		int id = nodeDefinition.getId();
		
		entity.setWorkspace(getWorkspace());
//		entity.setCaption(caption);
		entity.setDataTable(dataTable.getName());
//		entity.setDescription(description);
		entity.setIdColumn( dataTable.getPrimaryKeyColumn().getName() );
		entity.setInput(true);
		entity.setName(dataTable.getName());
		entity.setOriginalId(id);
		entity.setOverride(false);
		
//		entity.setLocationColumn(locationColumn);
		
		if ( nodeDefinition instanceof EntityDefinition ) {
			setCoordinateColumns(entity, dataTable);
		}

		Entity parentEntity = getParentEntity(nodeDefinition);
		if(parentEntity != null) {
			entity.setParent( parentEntity );
			entity.setParentIdColumn(dataTable.getParentKeyColumn().getName());
		}
		
		createVariables(entity, dataTable);
		
		return entity;
	}
	
	private void deleteNotOverriddenEntities() {
		Workspace ws = getWorkspace();
		psql()
			.delete(ENTITY)
			.where(ENTITY.WORKSPACE_ID.eq(ws.getId())
					.and(ENTITY.OVERRIDE.eq(false)
					.and(DSL.notExists(
							psql()
							.select(VARIABLE.ID)
							.from(VARIABLE)
							.where(VARIABLE.ENTITY_ID.eq(ENTITY.ID)
								.and(VARIABLE.OVERRIDE.eq(true)))
							)))
					).execute();
		Collection<Entity> notOverriddenEntities = ws.removeNotOverriddenEntities();
		log().debug(String.format("Removed %d not overridden entities", notOverriddenEntities.size()));
	}

	private void merge(Entity oldEntity, Entity newEntity) {
		psql()
			.delete(VARIABLE)
			.where(VARIABLE.ENTITY_ID.eq(oldEntity.getId())
				.and(VARIABLE.OVERRIDE.eq(false))
				).execute();
		Collection<Variable<?>> notOverriddenVariables = oldEntity.getNotOverriddenVariables();
		oldEntity.removeVariables(notOverriddenVariables);
		for (Variable<?> var : newEntity.getVariables()) {
			Integer originalId = var.getOriginalId();
			if ( originalId != null ) {
				Variable<?> oldVar = oldEntity.getVariableByOriginalId(originalId);
				if ( oldVar != null ) {
					oldEntity.removeVariable(oldVar);
				}
			}
			oldEntity.addVariable(var);
		}
	}

	private void createVariables(Entity entity, DataTable dataTable) {
		NodeDefinition nodeDefinition = dataTable.getNodeDefinition();
		if ( nodeDefinition instanceof EntityDefinition ) {
			EntityDefinition entityDefinition = (EntityDefinition) nodeDefinition;
			List<AttributeDefinition> childrenAttrDefns = getChildrenAttributeDefinitions(entityDefinition);
			for (AttributeDefinition attrDefn : childrenAttrDefns) {
				List<DataColumn> dataColumns = dataTable.getDataColumns(attrDefn);
				for (DataColumn dataColumn : dataColumns) {
					createVariable(entity, dataColumn);
				}
			}
		} else {
			//TODO
		}
	}

	private void createVariable(Entity entity, DataColumn column) {
		Variable<?> v = null;
		String entityName = entity.getName();
		AttributeDefinition attrDefn = column.getAttributeDefinition();
		if ( attrDefn instanceof BooleanAttributeDefinition ) {
			v = new BinaryVariable();
			((BinaryVariable) v).setDisaggregate(! (column instanceof PrimaryKeyColumn));
		} else if ( attrDefn instanceof CodeAttributeDefinition) {
			v = new MultiwayVariable();
			v.setScale(Scale.NOMINAL);
			((MultiwayVariable) v).setMultipleResponse(attrDefn.isMultiple());
			((MultiwayVariable) v).setDisaggregate(! (column instanceof PrimaryKeyColumn));
			CodeList list = ((CodeAttributeDefinition) attrDefn).getList();
			((CategoricalVariable<?>) v).setDegenerateDimension(list.isExternal());
		} else if ( attrDefn instanceof NumberAttributeDefinition ) {
			v = new QuantitativeVariable();
			v.setScale(Scale.RATIO);
			//TODO set unit...
		} else if ( attrDefn instanceof TaxonAttributeDefinition ) {
			v = new MultiwayVariable();
			v.setScale(Scale.NOMINAL);
			NodeDefinition columnNodeDefn = column.getNodeDefinition();
			String fieldName = columnNodeDefn.getName();
			String name = fieldName.equals(TaxonAttributeDefinition.CODE_FIELD_NAME ) ? 
					SPECIES_CODE_VAR_NAME: SPECIES_SCIENT_NAME_VAR_NAME;
			v.setName(generateVariableName(entityName, name));
			((MultiwayVariable) v).setDegenerateDimension(true);
		}
		if ( v != null ) {
			v.setDimensionTable(entityName + "_" + v.getName() + DIMENSION_TABLE_SUFFIX);
			v.setInputValueColumn(column.getName());
			if ( v.getName() == null ) {
				v.setName(generateVariableName(entityName, column.getName()));
			}
			v.setOriginalId(attrDefn.getId());
			v.setOutputValueColumn(generateOutputValueColumnName(entityName, column.getName()));
			v.setSortOrder(entity.getVariableNextSortOrder());
			entity.addVariable(v);
		}
	}

	private void setCoordinateColumns(Entity entity, DataTable dataTable) {
		EntityDefinition entityDefinition = (EntityDefinition) dataTable.getNodeDefinition();
		CoordinateAttributeDefinition coordinateAttrDefn = getChildCoordinateAttributeDefinition(entityDefinition);
		if ( coordinateAttrDefn != null ) {
			// SRS
			FieldDefinition<?> srsField = coordinateAttrDefn.getFieldDefinition(CoordinateAttributeDefinition.SRS_FIELD_NAME);
			DataColumn srsCol = dataTable.getDataColumn(srsField);
			if ( srsCol != null ) {
				entity.setSrsColumn(srsCol.getName());
			}
			// X
			FieldDefinition<?> xField = coordinateAttrDefn.getFieldDefinition(CoordinateAttributeDefinition.X_FIELD_NAME);
			DataColumn xCol = dataTable.getDataColumn(xField);
			if ( xCol != null ) {
				entity.setXColumn(xCol.getName());
			}
			// Y
			FieldDefinition<?> yField = coordinateAttrDefn.getFieldDefinition(CoordinateAttributeDefinition.X_FIELD_NAME);
			DataColumn yCol = dataTable.getDataColumn(yField);
			if ( srsCol != null ) {
				entity.setYColumn(yCol.getName());
			}
		}
	}
	
	/**
	 * Search for a {@link CoordinateAttributeDefinition} among the children {@link AttributeDefinition}
	 * or among descendants in nested single {@link EntityDefinition}
	 * 
	 * @param entityDefn
	 * @return
	 */
	private CoordinateAttributeDefinition getChildCoordinateAttributeDefinition(EntityDefinition entityDefn) {
		List<AttributeDefinition> childrenAttrDefns = getChildrenAttributeDefinitions(entityDefn);
		for (AttributeDefinition attrDefn : childrenAttrDefns) {
			if ( attrDefn instanceof CoordinateAttributeDefinition ) {
				return (CoordinateAttributeDefinition) attrDefn;
			}
		}
		return null;
	}
	
	/**
	 * Returns single attribute definitions descendants of the entity definition specified
	 * that are children of the entity or that are inside single entity definitions.
	 * 
	 * @param entityDefinition
	 * @return
	 */
	private List<AttributeDefinition> getChildrenAttributeDefinitions(EntityDefinition entityDefinition) {
		List<AttributeDefinition> result = new ArrayList<AttributeDefinition>();
		Stack<EntityDefinition> stack = new Stack<EntityDefinition>();
		stack.push(entityDefinition);
		while ( ! stack.isEmpty() ) {
			EntityDefinition e = stack.pop();
			List<NodeDefinition> childDefinitions = e.getChildDefinitions();
			for (NodeDefinition nodeDefn : childDefinitions) {
				if ( ! nodeDefn.isMultiple() ) {
					if ( nodeDefn instanceof AttributeDefinition ) {
						result.add((AttributeDefinition) nodeDefn);
					} else if ( nodeDefn instanceof EntityDefinition ) {
						stack.push((EntityDefinition) nodeDefn);
					}
				}
			}
		}
		return result;
	}

	private Entity getParentEntity(NodeDefinition defn) {
		EntityDefinition parentDefn = defn.getParentEntityDefinition();
		if( parentDefn == null ) {
			return null;
		} else if(parentDefn.isMultiple()){
			return entitiesByEntityDefinitionId.get( parentDefn.getId() );
		} else {
			return getParentEntity(parentDefn);
		}
	}
	
	private RelationalSchema generateSchema(Survey survey) {
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		try {
			RelationalSchema schema = rdbGenerator.generateSchema(survey, getWorkspace().getInputSchema());
			return schema;
		} catch (CollectRdbException e) {
			throw new RuntimeException("Unable to generate schema" , e);
		}
	}
	
	private String generateVariableName(String entityName, String columnName) {
		String name = columnName;
		if ( variableNames.contains(name) ) {
			//name = ENTITYNAME_COLUMNNAME
			name = entityName + "_" + name;
			String baseName = name;
			int count = 0;
			while ( variableNames.contains(name) ) {
				//name = ENTITYNAME_COLUMNNAME#
				name = baseName + (++count);
			}
		}
		variableNames.add(name);
		return name;
	}
	
	private String generateOutputValueColumnName(String entityName, String columnName) {
		String name = columnName;
		if ( outputValueColumnNames.contains(name) ) {
			//name = ENTITYNAME_COLUMNNAME
			name = entityName + "_" + name;
			String baseName = name;
			int count = 0;
			while ( outputValueColumnNames.contains(name) ) {
				//name = ENTITYNAME_COLUMNNAME#
				name = baseName + (++count);
			}
		}
		outputValueColumnNames.add(name);
		return name;
	}
	
	protected void printToLog(List<Entity> entityList) {
		// TODO print to debug log instead
		for (Entity entity : entityList) {
			List<Variable<?>> vars = entity.getVariables();
			for (Variable<?> var : vars) {
				System.out.printf("%s.%s (%s)%n", entity.getName(), var.getName(), var.getScale());
			}
		}
	}

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

}

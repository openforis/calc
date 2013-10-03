/**
 * 
 */
package org.openforis.calc.collect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceDao;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EntityDao;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.TextVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.persistence.jooq.tables.EntityTable;
import org.openforis.collect.model.CollectSurvey;
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
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class CollectMetadataImportTask extends Task {

	private static final String SPECIES_CODE_VAR_NAME = "species_code";
	private static final String SPECIES_SCIENT_NAME_VAR_NAME = "species_scient_name";
	private static final String DIMENSION_TABLE_FORMAT = "%s_%s_dim";

	@Autowired
	private WorkspaceDao workspaceDao;
	@Autowired
	private EntityDao entityDao;
	@Autowired
	private VariableDao variableDao;
	
	@Override
	public String getName() {
		return "Import metadata";
	}
	
	//transient variables
	private Map<Integer, Entity> entitiesByEntityDefinitionId;
	private Set<String> variableNames;
	private Set<String> outputValueColumnNames;

	@Override
	protected long countTotalItems() {
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		Schema schema = survey.getSchema();
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(schema.getRootEntityDefinitions());
		int totalNodes = 0;
		while ( ! stack.isEmpty() ) {
			NodeDefinition nodeDefn = stack.pop();
			if ( nodeDefn instanceof EntityDefinition ) {
				stack.addAll(((EntityDefinition) nodeDefn).getChildDefinitions());
			}
			totalNodes++;
		}
		return totalNodes;
	}
	
	@Override
	protected void execute() throws Throwable {
		entitiesByEntityDefinitionId = new HashMap<Integer, Entity>();
		variableNames = new HashSet<String>();
		outputValueColumnNames = new HashSet<String>();

		clearWorkspace();
		
		Workspace ws = getWorkspace();
		
		List<Entity> entities = createEntitiesFromSchema();
		
		ws.setEntities(entities);
		
		printToLog(entities);
		
		workspaceDao.save(ws);
		
		//TODO children entity ids not updated after save...check this
		Workspace reloaded = workspaceDao.find(ws.getId());
		ws.setEntities(reloaded.getEntities());
	}
	
	private void clearWorkspace() {
		int workspaceId = getWorkspace().getId();
		psql()
			.delete(EntityTable.ENTITY)
			.where(EntityTable.ENTITY.WORKSPACE_ID.eq(workspaceId))
			.execute();
		getWorkspace().setEntities(null);
	}

	private List<Entity> createEntitiesFromSchema() throws IdmlParseException {
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		
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
				incrementItemsProcessed();
			}
		});
		return new ArrayList<Entity>(entitiesByEntityDefinitionId.values());
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
			//TODO handle import multiple attributes
		}
	}

	private void createVariable(Entity entity, DataColumn column) {
		Variable<?> v = null;
		String entityName = entity.getName();
		NodeDefinition columnNodeDefn = column.getNodeDefinition();
		String columnNodeDefnNam = columnNodeDefn.getName();
		AttributeDefinition attrDefn = column.getAttributeDefinition();
		if ( attrDefn instanceof BooleanAttributeDefinition && 
				columnNodeDefnNam.equals(BooleanAttributeDefinition.VALUE_FIELD) ) {
			v = new BinaryVariable();
			((BinaryVariable) v).setDisaggregate(! (column instanceof PrimaryKeyColumn));
		} else if ( attrDefn instanceof CodeAttributeDefinition &&
				columnNodeDefnNam.equals(CodeAttributeDefinition.CODE_FIELD)) {
			v = new MultiwayVariable();
			v.setScale(Scale.NOMINAL);
			((MultiwayVariable) v).setMultipleResponse(attrDefn.isMultiple());
			((MultiwayVariable) v).setDisaggregate(! (column instanceof PrimaryKeyColumn));
			CodeList list = ((CodeAttributeDefinition) attrDefn).getList();
			((CategoricalVariable<?>) v).setDegenerateDimension(list.isExternal());
		} else if ( attrDefn instanceof DateAttributeDefinition ) {
			v = new TextVariable();
			v.setScale(Scale.TEXT);
		} else if ( attrDefn instanceof NumberAttributeDefinition &&
				columnNodeDefnNam.equals(NumberAttributeDefinition.VALUE_FIELD)) {
			v = new QuantitativeVariable();
			v.setScale(Scale.RATIO);
			//TODO set unit...
		} else if ( attrDefn instanceof TaxonAttributeDefinition &&
				(columnNodeDefnNam.equals(TaxonAttributeDefinition.CODE_FIELD_NAME) ||
					columnNodeDefnNam.equals(TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME) ) ) {
			v = new MultiwayVariable();
			v.setScale(Scale.NOMINAL);
			String fieldName = columnNodeDefnNam;
			String name = fieldName.equals(TaxonAttributeDefinition.CODE_FIELD_NAME ) ? 
					SPECIES_CODE_VAR_NAME: SPECIES_SCIENT_NAME_VAR_NAME;
			v.setName(generateVariableName(entityName, name));
			((MultiwayVariable) v).setDegenerateDimension(true);
		} else if ( attrDefn instanceof TextAttributeDefinition && 
				((TextAttributeDefinition) attrDefn).getType() == Type.SHORT ) {
			v = new TextVariable();
			v.setScale(Scale.TEXT);
		} else if ( attrDefn instanceof TimeAttributeDefinition ) {
			v = new TextVariable();
			v.setScale(Scale.TEXT);
		}
		if ( v != null ) {
			v.setInputValueColumn(column.getName());
			if ( v.getName() == null ) {
				v.setName(generateVariableName(entityName, column.getName()));
			}
			if ( ! (v instanceof CategoricalVariable && ((CategoricalVariable<?>) v).isDegenerateDimension() ||
					v instanceof TextVariable ) ) {
				v.setDimensionTable(getDimensionTableName(entityName, v.getName()));
			}
			v.setOriginalId(attrDefn.getId());
			v.setOutputValueColumn(generateOutputValueColumnName(entityName, column.getName()));
			v.setSortOrder(entity.getVariableNextSortOrder());
			entity.addVariable(v);
		}
	}

	private String getDimensionTableName(String entityName, String variableName) {
		String result = String.format(DIMENSION_TABLE_FORMAT, entityName, variableName);
		return result;
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
			FieldDefinition<?> yField = coordinateAttrDefn.getFieldDefinition(CoordinateAttributeDefinition.Y_FIELD_NAME);
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

}

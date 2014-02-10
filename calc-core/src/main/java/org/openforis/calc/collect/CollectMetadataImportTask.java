/**
 * 
 */
package org.openforis.calc.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceDao;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EntityDao;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.TextVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.RelationalSchema;
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
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class CollectMetadataImportTask extends Task {

//	private static final QName CALC_SAMPLING_UNIT_ANNOTATION = new QName("http://www.openforis.org/calc/1.0/calc", "samplingUnit");
//	private static final String DIMENSION_TABLE_FORMAT = "%s_%s_dim";

	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Autowired
	private EntityDao entityDao;
	
	@Autowired
	private VariableDao variableDao;
	
	@Autowired
	private CalculationStepDao calculationStepDao;
	
	@Override
	public String getName() {
		return "Import metadata";
	}
	
	//transient variables
	private Map<Integer, Entity> entitiesByEntityDefinitionId;
	private Set<String> variableNames;

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
		
		List<Entity> newEntities = createEntitiesFromSchema();

		applyChangesToWorkspace(newEntities);
		
		printToLog(newEntities);
		
	}

	private void applyChangesToWorkspace(List<Entity> newEntities) {
		Workspace ws = getWorkspace();
		//remove deleted entities
		Collection<Entity> entitiesToBeRemoved = new HashSet<Entity>();
		for (Entity oldEntity : ws.getEntities()) {
			Entity newEntity = getEntityByOriginalId(newEntities, oldEntity.getOriginalId());
			if ( newEntity == null ) {
				entitiesToBeRemoved.add(oldEntity);
			}
		}
		ws.removeEntities(entitiesToBeRemoved);
		
		//apply changes to existing entities
		for (Entity oldEntity : ws.getEntities()) {
			Entity newEntity = getEntityByOriginalId(newEntities, oldEntity.getOriginalId());
			if ( newEntity != null ) {
				applyChangesToEntity(oldEntity, newEntity);
			}
		}
		
		//add new entities
		for (Entity newEntity : newEntities) {
			Entity oldEntity = ws.getEntityByOriginalId(newEntity.getOriginalId());
			if ( oldEntity == null ) {
				ws.addEntity(newEntity);
			}
		}
		
		//save workspace
		workspaceDao.save(ws);
		
		//TODO children entity ids not updated after save...check this
		Workspace reloaded = workspaceDao.find(ws.getId());
		ws.setEntities(reloaded.getEntities());
	}

	private void applyChangesToEntity(Entity oldEntity, Entity newEntity) {
		//update entity attributes
		oldEntity.setCaption(newEntity.getCaption());
		oldEntity.setDataTable(newEntity.getDataTable());
		oldEntity.setDescription(newEntity.getDescription());
		oldEntity.setIdColumn(newEntity.getIdColumn());
		oldEntity.setLocationColumn(newEntity.getLocationColumn());
		oldEntity.setName(newEntity.getName());
		oldEntity.setParentIdColumn(newEntity.getParentIdColumn());
//		oldEntity.setSamplingUnit(newEntity.isSamplingUnit());
		oldEntity.setSrsColumn(newEntity.getSrsColumn());
		oldEntity.setUnitOfAnalysis(newEntity.isUnitOfAnalysis());
		oldEntity.setXColumn(newEntity.getXColumn());
		oldEntity.setYColumn(newEntity.getYColumn());
		
		//remove deleted variables
		Collection<Variable<?>> variablesToBeRemoved = new HashSet<Variable<?>>();
		for (Variable<?> oldVariable : oldEntity.getVariables()) {
			Integer oldVariableOrigId = oldVariable.getOriginalId();
			if ( oldVariableOrigId != null ) {
				Variable<?> newVariable = newEntity.getVariableByOriginalId(oldVariableOrigId);
				if ( newVariable == null ) {
					variablesToBeRemoved.add(oldVariable);
				}
			}
		}
		oldEntity.removeVariables(variablesToBeRemoved);
		
		//apply changes to existing variables
		for (Variable<?> oldVariable : oldEntity.getVariables()) {
			Integer oldVariableOrigId = oldVariable.getOriginalId();
			if ( oldVariableOrigId != null ) {
				Variable<?> newVariable = newEntity.getVariableByOriginalId(oldVariableOrigId);
				applyChangesToVariable(oldVariable, newVariable);
			}
		}
		
		//add new variables
		for (Variable<?> newVariable : newEntity.getVariables()) {
			Variable<?> oldVariable = oldEntity.getVariableByOriginalId(newVariable.getOriginalId());
			if ( oldVariable == null ) {
				oldEntity.addVariable(newVariable);
			}
		}
	}
	
	private void applyChangesToVariable(Variable<?> oldVariable, Variable<?> newVariable) {
		oldVariable.setCaption(newVariable.getCaption());
		setDefaultValue(oldVariable, newVariable);
		oldVariable.setDescription(newVariable.getDescription());
		oldVariable.setDimensionTable(newVariable.getDimensionTable());
		//TODO update variable name and inputValueColumn: handle taxon attribute variables (2 variables per each attribute definition)
//		oldVariable.setInputValueColumn(newVariable.getInputValueColumn());
		//oldVariable.setName(newVariable.getName());
		oldVariable.setOutputValueColumn(newVariable.getOutputValueColumn());
		if ( newVariable instanceof MultiwayVariable ) {
			((MultiwayVariable) oldVariable).setInputCategoryIdColumn(((MultiwayVariable) newVariable).getInputCategoryIdColumn());
			((MultiwayVariable) oldVariable).setDimensionTable(((MultiwayVariable) newVariable).getDimensionTable());
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Object> void setDefaultValue(Variable<?> oldVariable,
			Variable<?> newVariable) {
		((Variable<T>) oldVariable).setDefaultValue((T) newVariable.getDefaultValue());
	}

	private Entity getEntityByOriginalId(List<Entity> entities, int originalId) {
		for (Entity entity : entities) {
			if ( originalId == entity.getOriginalId().intValue() )  {
				return entity;
			}
		}
		return null;
	}

	private List<Entity> createEntitiesFromSchema() throws IdmlParseException {
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		
		final RelationalSchema relationalSchema = ((CollectJob) getJob()).getInputRelationalSchema();
		
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
		entity.setCaption(nodeDefinition.getLabel(NodeLabel.Type.INSTANCE));
		entity.setDescription(nodeDefinition.getDescription());
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
		
//		boolean samplingUnit = Boolean.parseBoolean(nodeDefinition.getAnnotation(CALC_SAMPLING_UNIT_ANNOTATION));
//		entity.setSamplingUnit(samplingUnit);
		
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
			MultiwayVariable multiwayVar = (MultiwayVariable) v;
			multiwayVar.setScale(Scale.NOMINAL);
			multiwayVar.setMultipleResponse(attrDefn.isMultiple());
			multiwayVar.setDisaggregate(! (column instanceof PrimaryKeyColumn));
			CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) attrDefn;
			CodeList list = codeAttrDefn.getList();
			multiwayVar.setDegenerateDimension(list.isExternal());
			
			if ( ! multiwayVar.isDegenerateDimension() ) {
				//set dimension table and input category id column
				RelationalSchema inputRelationalSchema = ((CollectJob) getJob()).getInputRelationalSchema();
				
				DataTable table = inputRelationalSchema.getDataTable(codeAttrDefn.getParentEntityDefinition());
				CodeValueFKColumn fk = table.getForeignKeyCodeColumn(codeAttrDefn);
				if ( fk != null ) {
					multiwayVar.setInputCategoryIdColumn(fk.getName());
				}
				
				CodeTable codeListTable = inputRelationalSchema.getCodeListTable( list, codeAttrDefn.getListLevelIndex() );
				
				multiwayVar.setDimensionTable( codeListTable.getName() );
			}
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
			String name = column.getName();
			v.setName(generateVariableName(entityName, name));
			((MultiwayVariable) v).setDegenerateDimension(true);
		} else if ( attrDefn instanceof TextAttributeDefinition && 
				((TextAttributeDefinition) attrDefn).getType() == TextAttributeDefinition.Type.SHORT ) {
			v = new TextVariable();
			v.setScale(Scale.TEXT);
		} else if ( attrDefn instanceof TimeAttributeDefinition ) {
			v = new TextVariable();
			v.setScale(Scale.TEXT);
		}
		if ( v != null ) {
			if ( v.getName() == null ) {
				v.setName(generateVariableName(entityName, column.getName()));
			}
//			if ( ! (v instanceof CategoricalVariable && ((CategoricalVariable<?>) v).isDegenerateDimension() ||
//					v instanceof TextVariable ) ) {
//				v.setDimensionTable(getDimensionTableName(entityName, v.getName()));
//			}
			v.setCaption(attrDefn.getLabel(NodeLabel.Type.INSTANCE));
			v.setDescription(attrDefn.getDescription());
			v.setInputValueColumn(v.getName());
			v.setOutputValueColumn(v.getName());
			v.setOriginalId(attrDefn.getId());
			entity.addVariable(v);
		}
	}

//	private String getDimensionTableName(String entityName, String variableName) {
//		String result = String.format(DIMENSION_TABLE_FORMAT, entityName, variableName);
//		return result;
//	}

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

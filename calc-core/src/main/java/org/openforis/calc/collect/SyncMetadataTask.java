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
import org.openforis.collect.relational.model.CodeColumn;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataParentKeyColumn;
import org.openforis.collect.relational.model.DataPrimaryKeyColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 * @author G. Miceli
 *
 */
public class SyncMetadataTask extends Task {

	private static final String SPECIES_CODE_VAR_NAME = "species_code";
	private static final String SPECIES_SCIENT_NAME_VAR_NAME = "species_scient_name";

	private static final String DIMENSION_TABLE_SUFFIX = "_dim";

	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Autowired
	private CollectSurveyIdmlBinder collectSurveyIdmlBinder;

	// Transient states
	private Map<String, Entity> entitiesByName;
	private RelationalSchema schema;
	private Set<String> variableNames;
	private Set<String> outputValueColumnNames;

	@Override
	protected void execute() throws Throwable {
		this.entitiesByName = new HashMap<String, Entity>();
		this.variableNames = new HashSet<String>();
		this.outputValueColumnNames = new HashSet<String>();
		
		this.schema = generateSchema();
		
		// convert idm metadata into calc entities
		sync();
		
		Workspace ws = getWorkspace();
		workspaceDao.save(ws);
		
		//TODO children entity ids not updated after save...check this
		Workspace reloaded = workspaceDao.find(ws.getId());
		ws.setEntities(reloaded.getEntities());
	}

	private RelationalSchema generateSchema() throws IdmlParseException {
		Survey survey = loadSurvey();
		return generateSchema(survey);
	}

	protected Survey loadSurvey() throws IdmlParseException {
		// TODO get survey from input schema
		InputStream surveyIs = getClass().getClassLoader().getResourceAsStream("test.idm.xml");
		Survey survey = collectSurveyIdmlBinder.unmarshal(surveyIs);
		return survey;
	}

	private RelationalSchema generateSchema(Survey survey) {
		Workspace ws = getWorkspace();
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		RelationalSchema schema;
		try {
			schema = rdbGenerator.generateSchema(survey, ws.getInputSchema());
		} catch (CollectRdbException e) {
			throw new RuntimeException(e);
		}
		return schema;
	}
	

	/**
	 * Sync a RDB {@link RelationalSchema} into {@link Workspace} metadata
	 * 
	 * @param workspace
	 * @param schema
	 * @return 
	 * @return
	 */
	synchronized
	public List<Entity> sync() {
		// TODO sync and return updated Workspace and map of which items 
		// were new, modified or deleted  
		entitiesByName.clear();
		
		Workspace ws = getWorkspace();
		
		deleteNotOverriddenEntities();
		
		// Convert IDM metadata and RDB schema to Calc metadata
		int sortOrder = 1;
		List<Table<?>> tables = schema.getTables();
		for (Table<?> table : tables) {
			// Sync entities and variables
			if ( table instanceof DataTable ) {
				Entity newEntity = convert((DataTable) table);
				if ( newEntity != null ) {
					Entity entity;
					Entity oldEntity = ws.getEntityByOriginalId(newEntity.getOriginalId());
					if ( oldEntity == null ) {
						entity = newEntity;
					} else {
						merge(oldEntity, newEntity);
						entity = oldEntity;
					}
					entity.setSortOrder(sortOrder++);
					entitiesByName.put(entity.getName(), entity);
				}
			}
		}
		List<Entity> entities = new ArrayList<Entity>(entitiesByName.values());
		
		// Update metadata
		ws.setEntities(entities);
		
		printToLog(entities);
		
		return entities;
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

	protected void printToLog(List<Entity> entityList) {
		// TODO print to debug log instead
		for (Entity entity : entityList) {
			List<Variable<?>> vars = entity.getVariables();
			for (Variable<?> var : vars) {
				System.out.printf("%s.%s (%s)%n", entity.getName(), var.getName(), var.getScale());
			}
		}
	}

	/**
	 * Converts a RDB {@link DataTable} into a Calc {@link Entity}
	 * 
	 * @param workspace
	 * @param table
	 * @return
	 */
	private Entity convert(DataTable table) {
		NodeDefinition defn = table.getNodeDefinition();
		if ( defn instanceof EntityDefinition || 
			(defn instanceof AttributeDefinition && defn.isMultiple()) ) {
			
			Workspace workspace = getWorkspace();
			Entity e = new Entity();
			e.setName(table.getName());
			e.setWorkspace(workspace);
			e.setDataTable(table.getName());
			e.setInput(true); //TODO handle user defined or modified entities
			e.setOriginalId(defn.getId());
			
			DataPrimaryKeyColumn idColumn = table.getPrimaryKeyColumn();
			e.setIdColumn(idColumn.getName());
			
			DataParentKeyColumn parentIdColumn = table.getParentKeyColumn();
			e.setParentIdColumn(parentIdColumn == null ? null : parentIdColumn.getName());
			
			setCoordinateColumns(e, table);
			
			// Assign parent entity. 
			DataTable parentTable = table.getParent();
			if ( parentTable != null ) {
				Entity parentEntity = entitiesByName.get(parentTable.getName());
				if ( parentEntity == null ) {
					// This should never happen because RDB API generates schema with 
					// proper ordering of relational hierarchy (preordered DFS) 
					throw new IllegalStateException("Parent entity expected but not found");
				}
				e.setParent(parentEntity);
			}
			
			// Convert columns to variables
			int sortOrder = 1;
			List<Column<?>> columns = table.getColumns();
			for (Column<?> column : columns) {
				if ( column instanceof DataColumn ) {
					Variable<?> variable = convert((DataColumn) column, e);
					if ( variable != null ) {
						variable.setSortOrder(sortOrder++);
						e.addVariable(variable);
					}
				}
			}
			return e;
		} else {
			throw new IllegalArgumentException("Entity definition or multiple attribute definition associated to DataTable expected");
		}
	}

	private void setCoordinateColumns(Entity e, DataTable table) {
		NodeDefinition defn = table.getNodeDefinition();
		if ( defn instanceof EntityDefinition ) {
			List<Column<?>> cols = table.getColumns();
			boolean xSet = false, ySet = false, srsSet = false;
			
			for (Column<?> c : cols) {
				if ( c instanceof DataColumn ) {
					DataColumn dataCol = (DataColumn) c;
					NodeDefinition fieldDefn = dataCol.getNodeDefinition();
					String fieldName = fieldDefn.getName();
					AttributeDefinition attrDefn = dataCol.getAttributeDefinition();
					if ( attrDefn instanceof CoordinateAttributeDefinition ) {
						if ( CoordinateAttributeDefinition.X_FIELD_NAME.equals(fieldName) ) {
							e.setXColumn(c.getName());
							xSet = true;
						} else if ( CoordinateAttributeDefinition.Y_FIELD_NAME.equals(fieldName) ) {
							e.setYColumn(c.getName());
							ySet = true;
						} else if ( CoordinateAttributeDefinition.SRS_FIELD_NAME.equals(fieldName) ) {
							e.setSrsColumn(c.getName());
							srsSet = true;
						}
					}
					if ( xSet && ySet && srsSet ) {
						//set x, y, srs column according to the values of the first found CoordinateAttributeDefinition
						break;
					}
				}
			}
		}
	}

	/**
	 * Converts a {@link Column} related to a single attribute definition into a {@link Variable}.
	 * Columns are translated into a specific {@link Variable} type according to the associated {@link NodeDefinition} type.
	 * 
	 * @param column
	 * @param e 
	 * @return
	 */
	private Variable<?> convert(DataColumn column, Entity e) {
		Variable<?> v = null;
		if ( isValueColumn(column) ) {
			AttributeDefinition defn = column.getAttributeDefinition();
			if ( defn instanceof BooleanAttributeDefinition ) {
				v = new BinaryVariable();
				((BinaryVariable) v).setDisaggregate(! (column instanceof PrimaryKeyColumn));
			} else if ( defn instanceof CodeAttributeDefinition) {
				v = new MultiwayVariable();
				v.setScale(Scale.NOMINAL);
				((MultiwayVariable) v).setMultipleResponse(defn.isMultiple());
				((MultiwayVariable) v).setDisaggregate(! (column instanceof PrimaryKeyColumn));
				CodeList list = ((CodeAttributeDefinition) defn).getList();
				((CategoricalVariable<?>) v).setDegenerateDimension(list.isExternal());
			} else if ( defn instanceof NumberAttributeDefinition ) {
				v = new QuantitativeVariable();
				v.setScale(Scale.RATIO);
				//TODO set unit...
//				Unit defaultUnit = ((NumberAttributeDefinition) attributeDefn).getDefaultUnit();
//				((QuantitativeVariable) variable).setUnit(convert(defaultUnit));
			} else if ( defn instanceof TaxonAttributeDefinition ) {
				v = new MultiwayVariable();
				v.setScale(Scale.NOMINAL);
				NodeDefinition columnNodeDefn = column.getNodeDefinition();
				String fieldName = columnNodeDefn.getName();
				String name = fieldName.equals(TaxonAttributeDefinition.CODE_FIELD_NAME ) ? SPECIES_CODE_VAR_NAME: SPECIES_SCIENT_NAME_VAR_NAME;
				String generateVariableName = generateVariableName(e.getName(), name);
				v.setName(generateVariableName);
				((MultiwayVariable) v).setDegenerateDimension(true);
			}
			if ( v != null ) {
				String variableName = v.getName();
				if ( variableName == null ) {
					variableName = generateVariableName(e.getName(), column.getName());
					v.setName(variableName);
				}
				v.setInputValueColumn(column.getName());
				v.setOriginalId(defn.getId());
				String outputValueColumnName = generateOutputValueColumnName(e.getName(), column.getName());
				v.setOutputValueColumn(outputValueColumnName);
				v.setDimensionTable(e.getName() + "_" + v.getName() + DIMENSION_TABLE_SUFFIX);
			}
		}
		return v;
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

	//TODO move it to RDB Column?
	/**
	 * Returns true if the column contains values that can be used as a {@link Variable}
	 * 
	 * @param column
	 * @return
	 */
	private boolean isValueColumn(DataColumn column) {
		NodeDefinition columnFieldDefn = column.getNodeDefinition();
		AttributeDefinition defn = column.getAttributeDefinition();
		if ( defn instanceof AttributeDefinition ) {
			String fieldDefnName = columnFieldDefn.getName();
			if ( defn instanceof CodeAttributeDefinition &&
					fieldDefnName.equals(CodeAttributeDefinition.CODE_FIELD) &&
					column instanceof CodeColumn) {
				return true;
			} else if ( defn instanceof NumberAttributeDefinition &&
					fieldDefnName.equals(NumberAttributeDefinition.VALUE_FIELD) ) {
				return true;
			} else if ( defn instanceof BooleanAttributeDefinition ) {
				return true;
			} else if ( defn instanceof DateAttributeDefinition ) {
				return true;
			} else if ( defn instanceof TaxonAttributeDefinition && 
					( fieldDefnName.equals(TaxonAttributeDefinition.CODE_FIELD_NAME) ||
						fieldDefnName.equals(TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME) ) 
					) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}
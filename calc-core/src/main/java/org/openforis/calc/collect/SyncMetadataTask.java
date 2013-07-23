package org.openforis.calc.collect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.engine.JobContext;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceDao;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.CodeColumn;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 * @author G. Miceli
 *
 */
public class SyncMetadataTask extends Task {

	private static final String DIMENSION_TABLE_SUFFIX = "_dim";

	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Autowired
	private CollectSurveyIdmlBinder collectSurveyIdmlBinder;

	// Transient states
	private Map<String, Entity> entities;
	private RelationalSchema schema;

	@Override
	protected void execute() throws Throwable {
		this.entities = new HashMap<String, Entity>();
		this.schema = generateSchema();
		// convert into entities
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
		
		// Convert IDM metadata and RDB schema to Calc metadata
		List<Table<?>> tables = schema.getTables();
		for (Table<?> table : tables) {
			// Sync entities and variables
			if ( table instanceof DataTable ) {
				Entity entity = convert((DataTable) table);
				entity.setInput(true); //TODO handle user defined or modified entities
				if ( entity != null ) {
					entities.put(table.getName(), entity);
				}
			} else if ( table instanceof CodeTable ) {
				System.out.printf("CODE TABLE %s%n", table.getName());
			}
		}
		List<Entity> entityList = new ArrayList<Entity>(entities.values());
		
		Workspace workspace = getWorkspace();
		
		// Update metadata
		workspace.setEntities(entityList);
		
		printToLog(entityList);
		
		return entityList;
	}

	protected void printToLog(List<Entity> entityList) {
		// TODO print to debug log instead
		for (Entity entity : entityList) {
			List<Variable> vars = entity.getVariables();
			for (Variable var : vars) {
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
			JobContext context = getContext();
			Workspace workspace = context.getWorkspace();
			Entity e = new Entity();
			e.setName(table.getName());
			e.setWorkspace(workspace);
			e.setDataTable(table.getName());
			
			setCoordinateColumns(e, table);
			
			// Assign parent entity. 
			DataTable parentTable = table.getParent();
			if ( parentTable != null ) {
				Entity parentEntity = entities.get(parentTable.getName());
				if ( parentEntity == null ) {
					// This should never happen because RDB API generates schema with 
					// proper ordering of relational hierarchy (preordered DFS) 
					throw new IllegalStateException("Parent entity expected but not found");
				}
				e.setParent(parentEntity);
			}
			
			// Convert columns to variables
			List<Column<?>> columns = table.getColumns();
			for (Column<?> column : columns) {
				if ( column instanceof DataColumn ) {
					Variable variable = convert(column);
					if ( variable != null ) {
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
					NodeDefinition fieldDefn = ((DataColumn) c).getNodeDefinition();
					String fieldName = fieldDefn.getName();
					AttributeDefinition attrDefn = getAttributeDefinition(c);
					if ( attrDefn instanceof CoordinateAttributeDefinition ) {
						if ( "x".equals(fieldName) ) {
							e.setXColumn(c.getName());
							xSet = true;
						} else if ( "y".equals(fieldName) ) {
							e.setYColumn(c.getName());
							ySet = true;
						} else if ( "srs".equals(fieldName) ) {
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
	 * @return
	 */
	private Variable convert(Column<?> column) {
		AttributeDefinition defn = getAttributeDefinition(column);
		Variable v = null;
		if ( isValueColumn(column) ) {
			if ( defn instanceof BooleanAttributeDefinition ) {
				v = new BinaryVariable();
			} else if ( defn instanceof CodeAttributeDefinition) {
				v = new CategoricalVariable();
				v.setScale(Scale.NOMINAL);
				((CategoricalVariable) v).setMultipleResponse(defn.isMultiple());
			} else if ( defn instanceof NumberAttributeDefinition ) {
				v = new QuantitativeVariable();
				v.setScale(Scale.RATIO);
				//TODO set unit...
//				Unit defaultUnit = ((NumberAttributeDefinition) attributeDefn).getDefaultUnit();
//				((QuantitativeVariable) variable).setUnit(convert(defaultUnit));
			}
			if ( v != null ) {
				v.setName(column.getName());
				v.setValueColumn(column.getName());
				v.setInput(true);
				v.setDimensionTable(v.getName() + DIMENSION_TABLE_SUFFIX);
			}
		}
		return v;
	}
	
	//TODO move it to RDB Column?
	/**
	 * Returns true if the column contains values that can be used as a {@link Variable}
	 * 
	 * @param column
	 * @return
	 */
	private boolean isValueColumn(Column<?> column) {
		NodeDefinition columnFieldDefn = ((DataColumn) column).getNodeDefinition();
		AttributeDefinition defn = getAttributeDefinition(column);
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
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	//TODO move it to RDB Column?
	/**
	 * Returns the {@link AttributeDefinition} associated to the column.
	 * 
	 * @param column
	 * @return
	 */
	private AttributeDefinition getAttributeDefinition(Column<?> column) {
		NodeDefinition columnFieldDefn = ((DataColumn) column).getNodeDefinition();
		AttributeDefinition attributeDefn;
		if ( columnFieldDefn instanceof AttributeDefinition ) {
			attributeDefn = (AttributeDefinition) columnFieldDefn;
		} else {
			attributeDefn = (AttributeDefinition) columnFieldDefn.getParentDefinition();
		}
		return attributeDefn;
	}

	public synchronized void init() {
	}
	
	/*
	protected Unit<?> convert(org.openforis.idm.metamodel.Unit unit) {
		//TODO
		return null;
	}
	*/
}
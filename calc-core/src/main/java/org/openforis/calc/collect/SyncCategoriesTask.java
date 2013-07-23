package org.openforis.calc.collect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.CategoryDao;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.CodeColumn;
import org.openforis.collect.relational.model.CodeListCodeColumn;
import org.openforis.collect.relational.model.CodeListDescriptionColumn;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 * @author G. Miceli
 *
 */
public class SyncCategoriesTask extends Task {

	@Autowired
	private WorkspaceService workspaceService;
	
	@Autowired
	private CollectSurveyIdmlBinder collectSurveyIdmlBinder;
	
	@Autowired
	private CategoryDao categoryDao;

	private RelationalSchema schema;

	@Override
	protected void execute() throws Throwable {
		// TODO use CategoricalVariable metadata to identify and sync code list tables into Calc schema

		//TODO pass schema from outside?
		initSchema();
		
		Workspace ws = getWorkspace();
		
		List<Variable> vars = getVariables();
		for (Variable v : vars) {
			if ( v instanceof CategoricalVariable ) {
				CodeTable rdbCodeTable = getRDBCodeTable((CategoricalVariable) v);
				if ( rdbCodeTable != null ) { //TODO it should never be != null, ofc_sampling_design code list?
					List<Column<?>> columns = rdbCodeTable.getColumns();
					String codeColumnName = getColumnName(columns, CodeListCodeColumn.class);
					String descriptionColumnName = getColumnName(columns, CodeListDescriptionColumn.class);
					categoryDao.copyCodesIntoCategories(
							ws.getInputSchema(), ws.getOutputSchema(), 
							v.getId(),
							rdbCodeTable.getName(), codeColumnName, descriptionColumnName);
				}
			} else if ( v instanceof BinaryVariable ) {
				insertBooleanCategories((BinaryVariable) v);
			}
		}
	}

	protected void insertBooleanCategories(BinaryVariable v) {
		insertBooleanCategory(v, Boolean.TRUE);
		insertBooleanCategory(v, Boolean.FALSE);
		insertBooleanCategory(v, null);
	}

	protected void insertBooleanCategory(BinaryVariable v, Boolean value) {
		Category c = new Category();
		c.setVariable(v);
		c.setCode(value == null ? "N": value.booleanValue() ? "T": "F");
		c.setName(value == null ? "NA": value.booleanValue() ? "TRUE": "FALSE");
		categoryDao.save(c);
	}

	protected void initSchema() throws IdmlParseException {
		Workspace ws = getWorkspace();
		Survey survey = loadSurvey();
		// generate rdb schema
		this.schema = generateSchema(ws, survey);
	}
	
	protected CodeTable getRDBCodeTable(CategoricalVariable v) {
		Entity entity = v.getEntity();
		String entityDataTableName = entity.getDataTable();
		Table<?> entityRDBTable = getRDBTable(entityDataTableName);
		Column<?> valueRDBColumn = getRDBColumn(entityRDBTable, v.getValueColumn());
		if ( valueRDBColumn instanceof CodeColumn ) {
			NodeDefinition codeFieldDefn = ((CodeColumn) valueRDBColumn).getNodeDefinition();
			CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) codeFieldDefn.getParentDefinition();
			CodeList codeList = codeAttrDefn.getList();
			Integer codeListLevelIdx = codeList.isHierarchical() ? codeAttrDefn.getCodeListLevel(): null;
			CodeTable codeListTable = schema.getCodeListTable(codeList, codeListLevelIdx);
			return codeListTable;
		} else {
			throw new IllegalStateException("Expected CodeColum instance, found: " + valueRDBColumn.getClass().getName());
		}
	}

	//TODO move to RDB Table
	protected Column<?> getRDBColumn(Table<?> entityRDBTable, String name) {
		List<Column<?>> columns = entityRDBTable.getColumns();
		for (Column<?> column : columns) {
			if ( column.getName().equals(name) ) {
				return column;
			}
		}
		throw new IllegalArgumentException("Column not found: " + name);
	}

	//TODO move to RDB schema
	protected Table<?> getRDBTable(String name) {
		List<Table<?>> tables = schema.getTables();
		for (Table<?> table : tables) {
			if ( table.getName().equals(name) ) {
				return table;
			}
		}
		throw new IllegalArgumentException("Table not found: " + name);
	}
	
	protected List<Variable> getVariables() {
		List<Variable> result = new ArrayList<Variable>();
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();
			for (Variable v : variables) {
				result.add(v);
			}
		}
		return result;
	}
	
	protected String getColumnName(List<Column<?>> columns, Class<?> type) {
		for (Column<?> column : columns) {
			if ( type.isAssignableFrom(column.getClass()) ) {
				return column.getName();
			}
		}
		throw new IllegalArgumentException("Column of type " + type.getName() + " not found");
	}

	protected Survey loadSurvey() throws IdmlParseException {
		// TODO get survey from input schema
		InputStream surveyIs = getClass().getClassLoader().getResourceAsStream("test.idm.xml");
		Survey survey = collectSurveyIdmlBinder.unmarshal(surveyIs);
		return survey;
	}

	private RelationalSchema generateSchema(Workspace ws, Survey survey) {
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		RelationalSchema schema;
		try {
			schema = rdbGenerator.generateSchema(survey, ws.getInputSchema());
		} catch (CollectRdbException e) {
			throw new RuntimeException(e);
		}
		return schema;
	}
	
}
/**
 * 
 */
package org.openforis.calc.collect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.openforis.calc.engine.MetadataManager;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.TextVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.util.CodeListTables;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLabel.Type;
import org.openforis.idm.metamodel.CodeListLevel;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class CollectMetadataImportTask extends Task {

	private static final int SPECIES_CATEGORY_ORIGINAL_ID = -9999;

//	private static final QName CALC_SAMPLING_UNIT_ANNOTATION = new QName("http://www.openforis.org/calc/1.0/calc", "samplingUnit");
//	private static final String DIMENSION_TABLE_FORMAT = "%s_%s_dim";

	@Autowired
	private MetadataManager metadataManager;
	
	private File backupFile;
	
	@Override
	public String getName() {
		return "Import metadata";
	}
	
	//transient variables
	private LinkedHashMap<Integer, Entity> entitiesByOriginalId;
	private Set<String> variableNames;

	@Override
	protected long countTotalItems() {
		int totalItems = 0;
		//total items = schema nodes count + code lists count + species list count
		
		//schema nodes count
		CollectSurvey survey = getSurvey();
		Schema schema = survey.getSchema();
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(schema.getRootEntityDefinitions());
		while ( ! stack.isEmpty() ){
			NodeDefinition nodeDefn = stack.pop();
			if ( nodeDefn instanceof EntityDefinition ) {
				stack.addAll( ((EntityDefinition) nodeDefn).getChildDefinitions() );
			}
			totalItems ++;
		}
		
		//code lists count
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList codeList : codeLists) {
			if ( ! codeList.isExternal() ) {
				totalItems ++;
			}
		}
		
		//species lists count
		try {
			ZipFile zipFile = new ZipFile(backupFile);
			BackupFileExtractor fileExtractor = new BackupFileExtractor(zipFile);
			List<String> speciesFileNames = fileExtractor.listSpeciesEntryNames();
			totalItems += speciesFileNames.size();
		} catch ( Exception e ) {
			throw new RuntimeException("Error counting species lists: " + e.getMessage(), e);
		}
		
		return totalItems;
	}

	@Override
	protected void execute() throws Throwable {
		entitiesByOriginalId = new LinkedHashMap<Integer, Entity>();
		variableNames = new HashSet<String>();
		
		updateWorkspaceCategories();
		updateWorkspaceMetadata();
		
		saveWorkspace();
	}

	private void updateWorkspaceCategories() throws ZipException, IOException {
		Workspace ws = getWorkspace();
		CollectSurvey survey = getSurvey();
		String surveyLanguage = survey.getDefaultLanguage();

		List<CodeList> codeLists = survey.getCodeLists();
		
		List<Category> categories = new ArrayList<Category>( ws.getCategories() );

		//remove all existing input categories
		Iterator<Category> iterator = categories.iterator();
		while( iterator.hasNext() ){
			Category category = iterator.next();
			if( category.isInput() ){
				iterator.remove();
			}
		}
		
		//add input categories
		for( CodeList codeList : codeLists ) {
			if ( ! codeList.isExternal() ) {
				Category category = new Category();
				category.setOriginalId( codeList.getId() );
				category.setCaption( codeList.getLabel( Type.ITEM, surveyLanguage ) );
				category.setDescription( codeList.getDescription( surveyLanguage ) );
				category.setName( codeList.getName() );
				categories.add( category );
	
				//TODO support multiple hierarchies
				CategoryHierarchy hierarchy = new CategoryHierarchy();
				hierarchy.setName( codeList.getName() );
	
				List<CodeListLevel> codeListHierarchy = codeList.getHierarchy();
				if ( codeListHierarchy.isEmpty() ) {
					//when no code list hierarchy is specified, a default category level is created
					CategoryLevel level = createCategoryLevel( codeList, null );
					hierarchy.addLevel(level);
				} else {
					for (int levelIdx = 0; levelIdx < codeListHierarchy.size(); levelIdx++) {
						CategoryLevel level = createCategoryLevel( codeList, levelIdx );
						hierarchy.addLevel( level );
					}
				}
				category.addHierarchy( hierarchy );
	
				incrementItemsProcessed();
			}
		}
		
		//add species categories
		ZipFile zipFile = new ZipFile(backupFile);
		BackupFileExtractor fileExtractor = new BackupFileExtractor(zipFile);
		List<String> speciesFileNames = fileExtractor.listSpeciesEntryNames();
		for (String speciesFileName : speciesFileNames) {
			Category category = createSpeciesCategory(speciesFileName);
			categories.add( category );
			
			incrementItemsProcessed();
		}
		ws.setCategories( categories );
	}

	protected Category createSpeciesCategory(String speciesFileName) {
		String speciesListName = FilenameUtils.getBaseName(speciesFileName);
		Category category = new Category();
		category.setName( speciesListName );
		category.setOriginalId( SPECIES_CATEGORY_ORIGINAL_ID );
		CategoryHierarchy hierarchy = new CategoryHierarchy();
		hierarchy.setName( speciesListName );
		
		SpeciesCodeTable speciesCodeTable = new SpeciesCodeTable( speciesListName, getInputSchema().getName());
		
		CategoryLevel level = new CategoryLevel();
		
		level.setName( speciesListName );
		level.setCaptionColumn( speciesCodeTable.getScientificNameField().getName() );
		level.setCodeColumn( speciesCodeTable.getCodeField().getName() );
		level.setIdColumn( speciesCodeTable.getIdField().getName() );
		level.setRank( 1 );
		level.setTableName( speciesCodeTable.getName() );

		hierarchy.addLevel( level );
		
		category.addHierarchy( hierarchy );
		
		return category;
	}

	protected CategoryLevel createCategoryLevel(CodeList codeList, Integer levelIdx) {
		CategoryLevel level = new CategoryLevel();

		CodeListLevel codeListLevel = levelIdx == null ? null: codeList.getHierarchy().get( levelIdx );
		String name = codeListLevel == null ? codeList.getName(): codeListLevel.getName();

		String tableName = CodeListTables.getTableName(codeList, levelIdx);
		
		level.setCaptionColumn( CodeListTables.getLabelColumnName( tableName ) );
		level.setCodeColumn( CodeListTables.getCodeColumnName( tableName ) );
		level.setIdColumn( CodeListTables.getIdColumnName( tableName ) );
		level.setName( name );
		level.setRank( levelIdx == null ? 1: levelIdx + 1 );
		level.setTableName( tableName );
		
		return level;
	}

	/**
	 * This method updates the workspace entities: 
	 * it adds the new entities to the workspace removing the ones not present
	 * 
	 * @return
	 * @throws IdmlParseException 
	 */
	@Transactional
	public Workspace updateWorkspaceMetadata() throws IdmlParseException {
		Workspace ws = getWorkspace();
		List<Entity> entities = createEntities();
		
		printToLog( entities );
		
		//remove deleted entities
		Collection<Entity> entitiesToBeRemoved = new HashSet<Entity>();
		for (Entity oldEntity : ws.getEntities()) {
			Entity newEntity = getEntityByOriginalId( entities, oldEntity.getOriginalId());
			if ( newEntity == null ) {
				entitiesToBeRemoved.add(oldEntity);
			}
		}
		metadataManager.deleteEntities(entitiesToBeRemoved);
		
		//apply changes to existing entities
		for (Entity oldEntity : ws.getEntities()) {
			Entity newEntity = getEntityByOriginalId( entities, oldEntity.getOriginalId());
			if ( newEntity != null ) {
				applyChangesToEntity(oldEntity, newEntity);
			}
		}
		
		//add new entities
		for (Entity newEntity : entities) {
			Entity oldEntity = ws.getEntityByOriginalId(newEntity.getOriginalId());
			if ( oldEntity == null ) {
				addToParentEntity(newEntity);
				ws.addEntity( newEntity );
//				metadataManager.saveEntity(ws, newEntity);
			}
		}

		return ws;
	}
	
	private List<Entity> createEntities() throws IdmlParseException {
		CollectSurvey survey = getSurvey();
		final RelationalSchema relationalSchema = ((CollectSurveyImportJob) getJob()).getInputRelationalSchema();
		Schema schema = survey.getSchema();
		
		schema.traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition definition) {
				if ( definition.isMultiple() ) {
					Entity entity = createEntity( definition, relationalSchema );
					entity.setSortOrder(entitiesByOriginalId.size() + 1);
					entitiesByOriginalId.put( definition.getId(), entity);
				}
				incrementItemsProcessed();
			}
		});
		return new ArrayList<Entity>(entitiesByOriginalId.values());
	}

	private Entity createEntity( NodeDefinition nodeDefinition, RelationalSchema relationalSchema ){
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

		Entity parentEntity = getParentEntity( nodeDefinition );
		if ( parentEntity != null ) {
			parentEntity.addChild( entity );
			entity.setParentIdColumn( dataTable.getParentKeyColumn().getName() );
		}
		
		createVariables( entity, dataTable );
		
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
		String columnNodeDefnName = columnNodeDefn.getName();
		AttributeDefinition attrDefn = column.getAttributeDefinition();
		if ( attrDefn instanceof BooleanAttributeDefinition &&  columnNodeDefnName.equals(BooleanAttributeDefinition.VALUE_FIELD) ) {
			v = new BinaryVariable();
			((BinaryVariable) v).setDisaggregate( !(column instanceof PrimaryKeyColumn) );
		} else if ( attrDefn instanceof CodeAttributeDefinition && columnNodeDefnName.equals(CodeAttributeDefinition.CODE_FIELD) ){
			v = new MultiwayVariable();
			MultiwayVariable variable = (MultiwayVariable) v;
			
			variable.setScale(Scale.NOMINAL);
			variable.setMultipleResponse(attrDefn.isMultiple());
			variable.setDisaggregate(! (column instanceof PrimaryKeyColumn));
			CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) attrDefn;
			CodeList list = codeAttrDefn.getList();
			variable.setDegenerateDimension( list.isExternal() );
			
			if ( ! variable.getDegenerateDimension() ) {
				//set dimension table and input category id column
				RelationalSchema inputRelationalSchema = ((CollectSurveyImportJob) getJob()).getInputRelationalSchema();
				
				DataTable table = inputRelationalSchema.getDataTable(codeAttrDefn.getParentEntityDefinition());
				
				CodeValueFKColumn fk = table.getForeignKeyCodeColumn(codeAttrDefn);
				if ( fk != null ) {
					variable.setInputCategoryIdColumn(fk.getName());
				}
				
				Workspace ws = getWorkspace();
				String codeListTableName = CodeListTables.getTableName(list, codeAttrDefn.getListLevelIndex());
				CategoryLevel categoryLevel = ws.getCategoryLevelByTableName( codeListTableName );
				
				variable.setCategoryLevel( categoryLevel );
			}
		} else if ( attrDefn instanceof DateAttributeDefinition ) {
			v = new TextVariable();
			v.setScale(Scale.TEXT);
		} else if ( attrDefn instanceof NumberAttributeDefinition &&
				columnNodeDefnName.equals(NumberAttributeDefinition.VALUE_FIELD)) {
			v = new QuantitativeVariable();
			v.setScale( Scale.RATIO );
			//TODO set unit...
		} else if ( attrDefn instanceof TaxonAttributeDefinition &&
				(columnNodeDefnName.equals(TaxonAttributeDefinition.CODE_FIELD_NAME) ) ) {
			v = new MultiwayVariable();
			MultiwayVariable multiwayVar = (MultiwayVariable) v;
			multiwayVar.setScale(Scale.NOMINAL);
			multiwayVar.setMultipleResponse(attrDefn.isMultiple());
			
			//associate category level
			Workspace ws = getWorkspace();
			String taxonomy = ((TaxonAttributeDefinition) attrDefn).getTaxonomy();
			SpeciesCodeTable speciesCodeTable = new SpeciesCodeTable(taxonomy, getInputSchema().getName());
			CategoryLevel categoryLevel = ws.getCategoryLevelByTableName( speciesCodeTable.getName() );
			
			multiwayVar.setCategoryLevel( categoryLevel );
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
			v.setCaption( attrDefn.getLabel(NodeLabel.Type.INSTANCE) );
			v.setDescription( attrDefn.getDescription() );
			v.setInputValueColumn( v.getName() );
			v.setOutputValueColumn( v.getName() );
			v.setOriginalId( attrDefn.getId() );
			
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
		} else if ( parentDefn.isMultiple() ) {
			return entitiesByOriginalId.get( parentDefn.getId() );
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
	
	
	
	private Entity getEntityByOriginalId(List<Entity> entities, int originalId) {
		for (Entity entity : entities) {
			if ( originalId == entity.getOriginalId().intValue() )  {
				return entity;
			}
		}
		return null;
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
		oldEntity.setUnitOfAnalysis(newEntity.getUnitOfAnalysis());
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
		metadataManager.deleteVariables(variablesToBeRemoved);
		
		// apply changes to existing variables
		for (Variable<?> oldVariable : oldEntity.getVariables()) {
			Integer oldVariableOrigId = oldVariable.getOriginalId();
			if ( oldVariableOrigId != null ) {
				Variable<?> newVariable = newEntity.getVariableByOriginalId(oldVariableOrigId);
				
				applyChangesToVariable(oldVariable, newVariable);
				
//				metadataManager.saveVariable(oldVariable.getEntity(), oldVariable);
			}
		}
		
		// add new variables
		for (Variable<?> newVariable : newEntity.getVariables()) {
			Variable<?> oldVariable = oldEntity.getVariableByOriginalId(newVariable.getOriginalId());
			if ( oldVariable == null ) {
				oldEntity.addVariable( newVariable );
//				metadataManager.saveVariable(oldEntity, newVariable);
			}
		}
	}
	
	private void applyChangesToVariable(Variable<?> dest, Variable<?> from) {
		dest.setCaption(from.getCaption());
		dest.setDescription(from.getDescription());
		setDefaultValue(dest, from);
		
		if ( dest instanceof CategoricalVariable ) {
			((CategoricalVariable<?>) dest).setCategoryLevel( ((CategoricalVariable<?>) from).getCategoryLevel()  );
		}
//		oldVariable.setInputValueColumn(newVariable.getInputValueColumn());
		//oldVariable.setName(newVariable.getName());
		dest.setOutputValueColumn(from.getOutputValueColumn());
		if ( from instanceof MultiwayVariable ) {
			MultiwayVariable toVar = (MultiwayVariable) dest;
			MultiwayVariable fromVar = (MultiwayVariable) from;
			toVar.setDegenerateDimension(fromVar.getDegenerateDimension());
			toVar.setDisaggregate(fromVar.getDisaggregate());
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Object> void setDefaultValue(Variable<?> oldVariable,
			Variable<?> newVariable) {
		((Variable<T>) oldVariable).setDefaultValue((T) newVariable.getDefaultValueTemp());
	}

	private void addToParentEntity(Entity newEntity) {
		Workspace ws = newEntity.getWorkspace();
		Entity parent = newEntity.getParent();
		if ( parent != null ) {
			Integer parentOriginalId = parent.getOriginalId();
			if ( parentOriginalId != null ) {
				Entity persistedParent = ws.getEntityByOriginalId(parentOriginalId);
				if ( persistedParent != null && persistedParent != parent ) {
					persistedParent.addChild(newEntity);
				}
			}
		}
	}
	
	private void saveWorkspace() {
		Workspace workspace = getWorkspace(); 
		metadataManager.saveWorkspace( workspace );
		( (CollectSurveyImportJob) getJob() ).updateWorkspace( workspace );
	}

	protected void printToLog(List<Entity> entityList) {
		// TODO print to debug log instead
		for (Entity entity : entityList) {
			List<Variable<?>> vars = entity.getVariables();
			for ( Variable<?> var : vars ){
				System.out.printf("%s.%s (%s)%n", entity.getName(), var.getName(), var.getScale());
			}
		}
	}
	
	private CollectSurvey getSurvey() {
		CollectSurvey survey = ((CollectSurveyImportJob) getJob()).getSurvey();
		return survey;
	}

	public File getBackupFile() {
		return backupFile;
	}
	
	public void setBackupFile(File backupFile) {
		this.backupFile = backupFile;
	}

}

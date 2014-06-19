/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.jooq.util.postgres.information_schema.tables.Schemata;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepCategoryClassParameters;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.metadata.CategoryLevel.CategoryLevelValue;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.CategoryTable;
import org.openforis.calc.persistence.jooq.tables.daos.CategoryDao;
import org.openforis.calc.persistence.jooq.tables.daos.CategoryHierarchyDao;
import org.openforis.calc.persistence.jooq.tables.daos.CategoryLevelDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.ExtendedSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manager for category objects
 * @author Mino Togna
 */
@Component
public class CategoryManager {
	
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private CategoryHierarchyDao categoryHierarchyDao;
	@Autowired
	private CategoryLevelDao categoryLevelDao;
	@Autowired
	private Psql psql;
	
	public CategoryManager() {
	}
	
	@Transactional
	public void load( Workspace workspace ){
		List<Category> categories = categoryDao.fetchByWorkspaceId( workspace.getId().longValue() );
		for ( Category category : categories ){
			workspace.addCategory( category );
			
			List<CategoryHierarchy> hierarchies = this.categoryHierarchyDao.fetchByCategoryId( category.getId().longValue() );
			for ( CategoryHierarchy categoryHierarchy : hierarchies ){
				category.addHierarchy( categoryHierarchy );
				
				List<CategoryLevel> levels = categoryLevelDao.fetchByHierarchyId( categoryHierarchy.getId() );
				for ( CategoryLevel categoryLevel : levels){
					categoryHierarchy.addLevel( categoryLevel );
				}
			}
		}
	}
	
	@Transactional
	public void save( Workspace workspace ){
		List<Category> categories = workspace.getCategories();

		for ( Category category : categories ){
			if( categoryDao.exists( category ) ){
				categoryDao.update(category);
			} else {
				category.setId( psql.nextval(Sequences.CATEGORY_ID_SEQ).intValue() );
				categoryDao.insert(category);
			}
			
			List<CategoryHierarchy> hierarchies = category.getHierarchies();
			for ( CategoryHierarchy hierarchy : hierarchies ){
				
				hierarchy.setCategoryId( category.getId().longValue() );
				
				if( categoryHierarchyDao.exists( hierarchy ) ){
					categoryHierarchyDao.update(hierarchy);
				} else {
					hierarchy.setId( psql.nextval(Sequences.CATEGORY_HIERARCHY_ID_SEQ).intValue() );
					categoryHierarchyDao.insert(hierarchy);
				}
				
				List<CategoryLevel> levels = hierarchy.getLevels();
				for ( CategoryLevel level : levels ){
					
					level.setHierarchyId( hierarchy.getId() );
					
					if( categoryLevelDao.exists( level ) ){
						categoryLevelDao.update(level);
					} else {
						level.setId( psql.nextval(Sequences.CATEGORY_LEVEL_ID_SEQ).intValue() );
						categoryLevelDao.insert(level);
					}	
				}
			}
		}
	}
	
	@Transactional
	public void deleteInputCategories(Workspace workspace) {
		CategoryTable T = Tables.CATEGORY;
		
		psql
			.delete(T)
			.where( T.WORKSPACE_ID.eq(workspace.getId().longValue())
					.and( T.ORIGINAL_ID.isNotNull()) )
			.execute();
	}
	
	@Transactional
	public void deleteOutputCategories( Workspace workspace ) {
		ArrayList<Category> categories = new ArrayList<Category>( workspace.getCategories() );
		Iterator<Category> iterator = categories.iterator();
		while( iterator.hasNext() ){
			Category category = iterator.next();
			if( category.isUserDefined() ){
				
				for ( CategoryHierarchy hierarchy : category.getHierarchies() ) {
					for ( CategoryLevel level : hierarchy.getLevels() ) {
						DynamicTable<?> table = new DynamicTable<Record>( level.getTableName() , level.getSchemaName() );
						psql
							.dropTableIfExists(table)
							.execute();
						
						categoryLevelDao.delete( level );
					}
					categoryHierarchyDao.delete( hierarchy );
				}
				categoryDao.delete( category );
				iterator.remove();
			}
		}
		workspace.setCategories( categories );
		
	}
	
	/**
	 * Creates a new category and a table for each level
	 * @param workspace
	 * @param category
	 */
	@Transactional
	public void createCategory( Workspace workspace , Category category , List<CategoryLevelValue> values ) {
		
		category.setId( psql.nextval(Sequences.CATEGORY_ID_SEQ).intValue() );
		
		workspace.addCategory(category);
		
		categoryDao.insert(category);
		
		for (CategoryHierarchy categoryHierarchy : category.getHierarchies()) {
			categoryHierarchy.setId( psql.nextval(Sequences.CATEGORY_HIERARCHY_ID_SEQ).intValue() );
			categoryHierarchy.setCategory(category);
			categoryHierarchyDao.insert(categoryHierarchy);
			
			for (CategoryLevel categoryLevel : categoryHierarchy.getLevels()) {

				createCategoryLevelTable( workspace, categoryLevel , values );
				
				categoryLevel.setId( psql.nextval(Sequences.CATEGORY_LEVEL_ID_SEQ).intValue() );
				categoryLevel.setHierarchy( categoryHierarchy );
				categoryLevelDao.insert( categoryLevel );
			}
		}
	}
	
	private void createCategoryLevelTable( Workspace workspace , CategoryLevel level, List<CategoryLevelValue> values ) {
		
		ExtendedSchema extendedSchema = workspace.schemas().getExtendedSchema();
		
		createSchemaIfNotExists(extendedSchema);
		
		DynamicTable<Record> table = new DynamicTable<Record>( level.getName() , extendedSchema.getName() );
		Field<Long> idField = table.getIdField();
		Field<String> codeField = table.getVarcharField( "code" );
		Field<String> captionField = table.getVarcharField( "caption" );
		
		psql
			.createTable( table, table.fields() )
			.execute();
			
		psql
			.alterTable(table)
			.addPrimaryKey(table.getPrimaryKey())
			.execute();
		
		List<Query> queries = new ArrayList<Query>();
		
		for ( CategoryLevelValue value : values ) {
			InsertQuery<Record> insert = psql.insertQuery(table);
			
			insert.addValue( idField, value.getId() );
			insert.addValue( codeField, value.getCode() );
			insert.addValue( captionField, value.getCaption() );
			
			queries.add(insert);
		}
		psql.batch( queries ).execute();
	
		level.setCaptionColumn(captionField.getName());
		level.setCodeColumn(codeField.getName());
		level.setIdColumn(idField.getName());
		level.setTableName(table.getName());
		level.setSchemaName(extendedSchema.getName());
	}

	protected void createSchemaIfNotExists(ExtendedSchema extendedSchema) {
		Schemata schemata = Schemata.SCHEMATA;
		
		Integer count = psql
					.selectCount()
					.from( schemata )
					.where( schemata.SCHEMA_NAME.eq(extendedSchema.getName()) )
					.fetchOne( DSL.count() );
		
		if( count == 0 ) {
			psql
				.createSchema( extendedSchema )
				.execute();
		}
	}
	
	@Transactional
	public List<CategoryLevelValue> loadCategoryLevelValues( CategoryLevel categoryLevel ){
		DynamicTable<Record> table = new DynamicTable<Record>( categoryLevel.getTableName() , categoryLevel.getSchemaName() );
		
		Field<Long> idField = table.getLongField( categoryLevel.getIdColumn() );
		Field<String> codeField = table.getVarcharField( categoryLevel.getCodeColumn() );
		Field<String> captionField = table.getVarcharField( categoryLevel.getCaptionColumn() );
		
		Result<Record> records = psql.select().from( table ).fetch();

		List<CategoryLevelValue> values = new ArrayList<CategoryLevel.CategoryLevelValue>();
		for ( Record record : records ) {
			CategoryLevelValue value = new CategoryLevelValue( record.getValue(idField), record.getValue(codeField), record.getValue(captionField) );
			values.add(value);
		}
		
		return values;
	}
	
	public CategoryLevelValue loadCategoryLevelValue( CategoryLevel categoryLevel , String code ){
		DynamicTable<Record> table = new DynamicTable<Record>( categoryLevel.getTableName() , categoryLevel.getSchemaName() );
		
		Field<Long> idField = table.getLongField( categoryLevel.getIdColumn() );
		Field<String> codeField = table.getVarcharField( categoryLevel.getCodeColumn() );
		Field<String> captionField = table.getVarcharField( categoryLevel.getCaptionColumn() );
		
		Record record = psql
			.select()
			.from( table )
			.where( codeField.eq(code) )
			.fetchOne();

		CategoryLevelValue value = new CategoryLevelValue( record.getValue(idField), record.getValue(codeField), record.getValue(captionField) );
		
		return value;
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public JSONArray loadCategoryClasses( Workspace workspace , int categoryId ){
		Category category = workspace.getCategoryById( categoryId );
		CategoryHierarchy categoryHierarchy = category.getHierarchies().get(0);
		CategoryLevel level = categoryHierarchy.getLevels().get(0);
		
		DynamicTable<Record> table = new DynamicTable<Record>(level.getTableName(), level.getSchemaName());
		Field<Integer> idField = table.getIntegerField( level.getIdColumn() );
		Field<String> codeField = table.getVarcharField( level.getCodeColumn() );
		Field<String> captionField = table.getVarcharField( level.getCaptionColumn() );
	
		
		Result<Record> result = psql
			.select(table.fields())
			.from(table)
			.fetch();
		JSONArray array = new JSONArray();
		for (Record record : result) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", record.getValue(idField) );
			jsonObject.put("caption", record.getValue(captionField) );
			jsonObject.put("code", record.getValue(codeField) );
			
			array.add( jsonObject );
		}
		return array;
	}
	
	@Transactional
	public void importBackup( Workspace workspace, WorkspaceBackup workspaceBackup ) {
		Workspace workspaceToImport = workspaceBackup.getWorkspace();

		List<Category> categories = workspaceToImport.getCategories();
		for ( Category category : categories ) {
			if( category.isUserDefined() ){
				
				CategoryHierarchy hierarchy = category.getHierarchies().get(0);
				CategoryLevel level = hierarchy.getLevels().get(0);
				
				Integer oldCategoryId = category.getId();
				Integer oldCategoryLevelId = level.getId();
				
				List<CategoryLevelValue> values = workspaceBackup.getCategoryLevelValues().get( level.getId() );
				this.createCategory( workspace, category , values );
				
				// replace categoryId in calc step of type category  
				Integer newCategoryId = category.getId();
				
				List<CalculationStep> calculationSteps = workspaceToImport.getDefaultProcessingChain().getCalculationSteps();
				for ( CalculationStep calculationStep : calculationSteps ) {
					if( calculationStep.getType() == Type.CATEGORY ){
						Integer stepCategoryId = calculationStep.getParameters().getInteger( "categoryId" );
						
						if( stepCategoryId.equals(oldCategoryId) ){
							// replace category id parameter
							calculationStep.getParameters().setInteger( "categoryId" , newCategoryId );
//
//							// replace category class ids in calculation steps
//							List<CalculationStepCategoryClassParameters> categoryClassParameters = calculationStep.getCategoryClassParameters();
//							for ( CalculationStepCategoryClassParameters classParameters : categoryClassParameters ) {
//								String classCode = classParameters.getClassCode();
//								
//								CategoryLevelValue levelValue = this.loadCategoryLevelValue( level, classCode );
//								classParameters.setClassId( levelValue.getId().intValue() );
//								
//								Integer variableOriginalId = workspaceBackup.getVariableOriginalIds().get( classParameters.getVariableId() );
//								Variable<?> variable = workspace.getVariableByOriginalId( variableOriginalId );
//								classParameters.setVariableId( variable.getId() );
//							}
						}
					}
				}

				level = hierarchy.getLevels().get(0);
				Integer newCategoryLevelId = level.getId();
				// replace categoryId in user defined variables				
				Collection<Variable<?>> userDefinedVariables = workspaceToImport.getUserDefinedVariables();
				for ( Variable<?> variable : userDefinedVariables ) {
					if( variable.getCategoryLevelId() != null && oldCategoryLevelId.equals(variable.getCategoryLevelId().intValue()) ) {
						variable.setCategoryLevelId(newCategoryLevelId.longValue());
					}
				}
				
			}
		}
		
	}

}

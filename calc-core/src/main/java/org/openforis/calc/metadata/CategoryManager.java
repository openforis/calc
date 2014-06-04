/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Sequences;
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

		//remove persisted categories
		List<Category> persistedCategories = categoryDao.fetchByWorkspaceId( workspace.getId().longValue() );
		categoryDao.delete(persistedCategories);
		
		//save new categories
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
	public void createCategory(Workspace workspace, Category category , List<String> codes , List<String> captions ){
		createCategoryTable(workspace, category, codes, captions);
	
		category.setId( psql.nextval(Sequences.CATEGORY_ID_SEQ).intValue() );
		category.setWorkspace(workspace);
		categoryDao.insert(category);
		
		for (CategoryHierarchy categoryHierarchy : category.getHierarchies()) {
			categoryHierarchy.setId( psql.nextval(Sequences.CATEGORY_HIERARCHY_ID_SEQ).intValue() );
			categoryHierarchy.setCategory(category);
			categoryHierarchyDao.insert(categoryHierarchy);
			
			for (CategoryLevel categoryLevel : categoryHierarchy.getLevels()) {
				categoryLevel.setId( psql.nextval(Sequences.CATEGORY_LEVEL_ID_SEQ).intValue() );
				categoryLevel.setHierarchy( categoryHierarchy );
				categoryLevelDao.insert( categoryLevel );
			}
		}
		
		workspace.addCategory(category);
	}
	
	private void createCategoryTable(Workspace workspace, Category category , List<String> codes , List<String> captions ) {
		
		if( codes.size() != captions.size() ){
			throw new IllegalArgumentException( "codes and captions must have the same size" );
		}
		
		ExtendedSchema extendedSchema = workspace.schemas().getExtendedSchema();
		
		createSchemaIfNotExists(extendedSchema);
		
		DynamicTable<Record> table = new DynamicTable<Record>( category.getName() , extendedSchema.getName() );
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
		boolean defaultFound = false;
		for ( int i = 0 ; i < codes.size() ; i++ ) {
			
			String catCode = codes.get(i);
			String catCaption = captions.get(i);
		
			InsertQuery<Record> insert = psql.insertQuery(table);
			insert.addValue(idField, (long)(i+1) );
			insert.addValue(codeField,catCode);
			insert.addValue(captionField, catCaption);
			
			queries.add(insert);
			
			if( catCode.equals("-1") ){
				defaultFound = true;
			}
		}
		// insert default category (not available)
		if( !defaultFound ){
			// NA
			InsertQuery<Record> insert = psql.insertQuery(table);
			insert.addValue(idField, (long)-1);
			insert.addValue(codeField,"-1");
			insert.addValue(captionField, "NA");
			queries.add(insert);
		}
		psql.batch( queries ).execute();
	
		CategoryHierarchy hierarchy = category.getHierarchies().get(0);
		CategoryLevel level = hierarchy.getLevels().get(0);
		
		level.setCaptionColumn(captionField.getName());
		level.setCodeColumn(codeField.getName());
		level.setIdColumn(idField.getName());
		level.setTableName(table.getName());
		level.setSchemaName(extendedSchema.getName());
	}

	protected void createSchemaIfNotExists(ExtendedSchema extendedSchema) {
		DynamicTable<Record> schemata = new DynamicTable<Record>("schemata" ,"information_schema");
		Field<String> schemaName = schemata.getVarcharField("schema_name");

		Integer count = psql
					.selectCount()
					.from( schemata )
					.where( schemaName.eq(extendedSchema.getName()) )
					.fetchOne( DSL.count() );
		if( count == 0 ) {
			psql
				.createSchema(extendedSchema)
				.execute();
		}
	}
	
	@SuppressWarnings("unchecked")
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
	
}

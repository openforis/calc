/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SelectLimitStep;
import org.json.simple.JSONObject;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.CategoryDimensionTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * @author S. Ricci
 *
 */
public class VariableDao extends org.openforis.calc.persistence.jooq.tables.daos.VariableDao {
	
	@Autowired
	private Psql psql;

	public VariableDao(Configuration configuration) {
		super(configuration);
	}

	public long countCategoryClasses(CategoryDimensionTable table ) {
		
		Long count = psql
			.selectCount()
			.from(table)
			.fetchOne( 0 , Long.class );
		
		return count;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> getCategoryClasses(CategoryDimensionTable table ){
		
		List<JSONObject> categories = new ArrayList<JSONObject>(); 
		
		SelectLimitStep<Record2<String, String>> select = psql
			.select( table.getCodeField() , table.getCaptionField() )
			.from( table )
			.orderBy( table.getIdField() );
		 
		 Result<Record2<String, String>> result = select.fetch();
		
		for (Record2<String, String> record : result) {
			
			JSONObject json = new JSONObject();
			json.put( "code" , record.value1() );
			json.put( "caption" , record.value2() );
			
			categories.add( json );
		}
		
		return categories;
	}
	
	@Transactional
	public void loadByWorkspace( Workspace workspace ) {
		List<Entity> entities = workspace.getEntities();

		Integer[] entityIds = new Integer[entities.size()];
		for ( int i = 0; i < entities.size(); i++ ) {
			entityIds[i] = entities.get(i).getId();
		}

		psql
			.select()
			.from( Tables.VARIABLE )
			.where( Tables.VARIABLE.ENTITY_ID.in(entityIds) )
			.fetch( new VariableRecordMapper(workspace) );
	}
	
	@Transactional
	public void save( Variable<?>... variables ) {
		if( variables != null ) {
			
			for (int i = 0; i < variables.length; i++) {
				Variable<?> variable = variables[i];
				// set entityId
				Entity entity = variable.getEntity();
				if( entity == null ){
					throw new IllegalStateException( "Found detached variable" );
				}
				variable.setEntityId( entity.getId() );
				// set sortOrder
				variable.setSortOrder( i + 1 );
				if( variable instanceof CategoricalVariable<?> && ((CategoricalVariable<?>) variable).getCategoryLevel() != null ){
					CategoryLevel categoryLevel = ( (CategoricalVariable<?>) variable ).getCategoryLevel();
					variable.setCategoryLevelId( categoryLevel.getId().longValue() );
				}
				
				// insert or update variable
				if( variable.getId() == null ) {
					
					Long nextval = psql.nextval( Sequences.VARIABLE_ID_SEQ );
					int varId = nextval.intValue();
					variable.setId( varId );
					
					insert( variable );
				} else {
					update( variable );
				}
			}
		
		}

	}
	
}

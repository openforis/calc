/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SelectLimitStep;
import org.json.simple.JSONObject;
import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.CategoryDimensionTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Mino Togna
 * @author S. Ricci
 *
 */
@SuppressWarnings("rawtypes")
@Repository
public class VariableDao extends AbstractJpaDao<Variable> {
	
	@Autowired
	private DataSource dataSource;
	
	public long countCategories(CategoryDimensionTable table ){
		
		Long count = new Psql(dataSource)
			.selectCount()
			.from(table)
			.fetchOne( 0 , Long.class );
		
		return count;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> getCategories(CategoryDimensionTable table ){
		
		List<JSONObject> categories = new ArrayList<JSONObject>(); 
		
		 SelectLimitStep<Record2<String, String>> select = new Psql(dataSource)
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
	
}

/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SelectLimitStep;
import org.json.simple.JSONObject;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.VariableBase;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.CategoryDimensionTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * @author S. Ricci
 *
 */
@Repository
public class VariableDao extends org.openforis.calc.persistence.jooq.tables.daos.VariableDao {
	
	@Autowired
	private Psql psql;
	
	public long countCategoryClasses(CategoryDimensionTable table ){
		
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
		// "case when scale='TEXT' then 'T' when scale in ( 'RATIO','INTERVAL','OTHER') then 'Q' when scale='BINARY' then 'B' else 'C' end"
		List<VariableBase> vars = super.fetchByEntityId(entityIds);
		for (VariableBase variableBase : vars) {
			Variable<?> variable = null;

			switch (variableBase.getScale()) {
			case TEXT:
				variable = new TextVariable();
				break;
			case BINARY:
				variable = new BinaryVariable();
				break;
			case NOMINAL:
				variable = new MultiwayVariable();
				break;
			default:
				variable = new QuantitativeVariable();
			}

			try {
				BeanUtils.copyProperties(variable, variableBase);
			} catch (Exception e) {
				// it should never happens
				throw new IllegalStateException( "Unable to load variables" , e );
			}

			Entity entity = workspace.getEntityById(variable.getEntityId());
			entity.addVariable(variable);
		}
	}
	
	public void save( List<Variable<?>> variables ) {
		
	}
	
	
}

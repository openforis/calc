/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.jooq.tables.EntityTable;
import org.openforis.calc.persistence.jooq.tables.VariableTable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Task responsible for deleting collect data 
 * 
 * @author Mino Togna
 *
 */
public class DeleteCollectDataTask extends Task {
	
	@Autowired
	private CategoryManager categoryManager;
	
	@Autowired
	private Psql psql;
	
	@Override
	protected long countTotalItems() {
		return 5;
	}
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		Schemas schemas = workspace.schemas();
		
		List<Integer> entities = new ArrayList<Integer>();
		for (Entity entity : workspace.getEntities() ) {
			Integer entityId = entity.getId();
			entities.add(entityId);
		}
		psql
			.delete( VariableTable.VARIABLE )
			.where( VariableTable.VARIABLE.ENTITY_ID.in(entities) )
			.execute();
		incrementItemsProcessed();
		
		psql
			.delete( EntityTable.ENTITY )
			.where( EntityTable.ENTITY.WORKSPACE_ID.eq(workspace.getId()) )
			.execute();
		incrementItemsProcessed();
		
		categoryManager.deleteInputCategories( workspace );
		incrementItemsProcessed();
		
		psql
			.dropSchemaIfExistsCascade( schemas.getExtendedSchema() )
			.execute();
		incrementItemsProcessed();
		
		psql
			.dropSchemaIfExistsCascade( schemas.getDataSchema() )
			.execute();;
		incrementItemsProcessed();
	}
	
	@Override
	public String getName() {
		return "Delete "  + getWorkspace().getName() + " collect data";
	}

}

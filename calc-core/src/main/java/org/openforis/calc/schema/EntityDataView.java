package org.openforis.calc.schema;

import org.jooq.Schema;
import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityDataView extends DataTable {

	public static final String VIEW_SUFFIX = "_view";
	
	private static final long serialVersionUID = 1L;

	public EntityDataView(Entity entity, Schema schema) {
		super(entity, getViewName(entity), schema);
		createPrimaryKeyField();
		createParentIdField();
		//TODO: coordinates disabled now
		//createCoordinateFields(); 
		createQuantityFields(true);
		createTextFields();
	}

	@Override
	protected void createQuantityFields(boolean input,
			boolean variableAggregates) {
		Entity currentEntity = getEntity();
		while ( currentEntity != null ) {
			createQuantityFields(currentEntity, input, variableAggregates);
			currentEntity = currentEntity.getParent();
		}
	}
	
	@Override
	protected void createTextFields() {
		Entity currentEntity = getEntity();
		while ( currentEntity != null ) {
			createTextFields(currentEntity);
			currentEntity = currentEntity.getParent();
		}
	}
	
	public static String getViewName(Entity entity) {
		return entity.getDataTable() + VIEW_SUFFIX;
	}

}

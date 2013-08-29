package org.openforis.calc.schema;

import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class InputTable extends DataTable {

	private static final long serialVersionUID = 1L;

	public InputTable(Entity entity, InputSchema schema) {
		super(entity, entity.getDataTable(), schema);
		createPrimaryKeyField();
		createParentIdField();		
		createCategoryValueFields(entity, true);
		createQuantityFields(true);
		createCoordinateFields();
	}
}

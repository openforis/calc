package org.openforis.calc.schema;

import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class InputDataTable extends DataTable {

	private static final long serialVersionUID = 1L;

	public InputDataTable(Entity entity, InputSchema schema) {
		super(entity, entity.getDataTable(), schema, null);
		createCategoryFields(entity, true);
		createQuantityFields(true);
		createCoordinateFields();
	}
}

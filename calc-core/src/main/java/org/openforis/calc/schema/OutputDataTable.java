package org.openforis.calc.schema;

import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class OutputDataTable extends DataTable {

	private static final long serialVersionUID = 1L;

	protected OutputDataTable(Entity entity, String name, OutputSchema schema, DataTable sourceTable) {
		super(entity, name, schema, sourceTable);
		createCategoryFields(entity, false);
		createQuantityFields(false);
		createStratumIdField();
		createAoiIdFields();
		createCoordinateFields();
		createLocationField();		
	}

	OutputDataTable(Entity entity, OutputSchema schema, InputDataTable sourceTable) {
		this(entity, entity.getDataTable(), schema, sourceTable);
	}
}

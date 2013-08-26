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

	private InputDataTable sourceTable;

	protected OutputDataTable(Entity entity, String name, OutputSchema schema, InputDataTable sourceTable) {
		super(entity, name, schema);
		this.sourceTable = sourceTable;
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

	public InputDataTable getSourceTable() {
		return sourceTable;
	}

}

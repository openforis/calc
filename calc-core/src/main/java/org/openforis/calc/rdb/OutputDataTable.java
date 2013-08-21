package org.openforis.calc.rdb;

import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class OutputDataTable extends DataTable {

	private static final long serialVersionUID = 1L;

	private InputDataTable inputDataTable;
	
	public OutputDataTable(Entity entity, OutputSchema schema, InputDataTable inputDataTable) {
		super(entity, schema);
		this.inputDataTable = inputDataTable;
	}
	
	public InputDataTable getInputDataTable() {
		return inputDataTable;
	}
}

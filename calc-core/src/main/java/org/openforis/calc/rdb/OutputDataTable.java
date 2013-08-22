package org.openforis.calc.rdb;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.metadata.Entity;
import static org.jooq.impl.SQLDataType.INTEGER;

/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class OutputDataTable extends DataTable {

	private static final long serialVersionUID = 1L;
	public final TableField<Record, Integer> STRATUM_ID;

	private InputDataTable inputDataTable;
	
	public OutputDataTable(Entity entity, OutputSchema schema, InputDataTable inputDataTable) {
		super(entity, schema);
		this.inputDataTable = inputDataTable;
		createVariableFields(false);
		this.STRATUM_ID = createField("_stratum_id", INTEGER, this);
	}
	
	public InputDataTable getInputDataTable() {
		return inputDataTable;
	}
}

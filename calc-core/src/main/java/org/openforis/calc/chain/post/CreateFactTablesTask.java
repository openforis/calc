package org.openforis.calc.chain.post;

import java.util.Collection;

import org.jooq.SelectQuery;
import org.openforis.calc.engine.Task;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.OutputDataTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Creates and populates fact tables for entities marked "unit of analysis"
 * 
 * @author G. Miceli
 */
public final class CreateFactTablesTask extends Task {
	@Override
	protected void execute() throws Throwable {
		OutputSchema outputSchema = getOutputSchema();
		Collection<FactTable> factTables = outputSchema.getFactTables();
		for (FactTable factTable : factTables) {
			OutputDataTable sourceTable = (OutputDataTable) factTable.getSourceTable();
			SelectQuery<?> select = psql().selectQuery(sourceTable);
		}
		// TODO Auto-generated method stub
	}
}
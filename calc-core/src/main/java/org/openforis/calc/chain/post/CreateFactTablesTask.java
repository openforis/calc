package org.openforis.calc.chain.post;

import java.util.Collection;

import org.openforis.calc.engine.Task;
import org.openforis.calc.schema.FactTable;
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
//		SelectQuery<?> select = psql().selectQuery(table)
		// TODO Auto-generated method stub
	}
}
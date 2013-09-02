package org.openforis.calc.chain.pre;

import java.util.Collection;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.OutputTable;
import org.openforis.calc.schema.RolapSchema;

/**
 * Copy tables into output schema based on {@link Category}s
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz
 * @author M. Togna
 */
public final class CreateOutputTablesTask extends Task {
	
	@Override
	protected void execute() throws Throwable {
		RolapSchema rolapSchema = getRolapSchema();
		OutputSchema outputSchema = rolapSchema.getOutputSchema();
		Collection<OutputTable> tables = outputSchema.getOutputTables();
		for ( OutputTable table : tables ) {
			createOutputDataTable((OutputTable) table);
		}
	}
	
	private void createOutputDataTable(OutputTable outputTable) {
		InputTable inputTable = outputTable.getInputTable();
		
		if ( isDebugMode() ) {
			psql()
				.dropTableIfExists(outputTable)
				.execute();
		}
		
		// Copy table from input schema
		SelectQuery<?> select = psql().selectQuery(inputTable);
		for (Field<?> outputField : outputTable.fields()) {
			Field<?> inputField = outputTable.getInputField(outputField);
			String name = outputField.getName();
			DataType<?> type = outputField.getDataType();
			if ( inputField == null ) {
				// add null to select, cast and alias				
				select.addSelect(DSL.val(null).cast(type).as(name));
			} else {
				// add column to select, cast and alias
				select.addSelect(inputField.cast(type).as(name));
			}
		}
		
		psql()
			.createTable(outputTable)
			.as(select)
			.execute();
		
		// Add primary key constraint and index
		psql()
			.alterTable(outputTable)
			.addPrimaryKey(outputTable.getPrimaryKey())
			.execute();

		// Grant access to system user
		psql()
			.grant(Privilege.ALL)
			.on(outputTable)
			.to(getSystemUser())
			.execute();
		
	}
}
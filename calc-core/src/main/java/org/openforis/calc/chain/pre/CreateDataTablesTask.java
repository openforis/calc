package org.openforis.calc.chain.pre;

import java.util.List;

import org.jooq.Field;
import org.jooq.Select;
import org.jooq.Table;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.persistence.postgis.Psql.Privilege;
import org.openforis.calc.schema.InputDataTable;
import org.openforis.calc.schema.OutputDataTable;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.RolapSchema;

/**
 * Copy tables into output schema based on {@link Category}s
 * 
 * @author G. Miceli
 * @author A. Sanchez-Paus Diaz
 * @author M. Togna
 */
public final class CreateDataTablesTask extends Task {
	
	@Override
	protected void execute() throws Throwable {
		RolapSchema rolapSchema = getRolapSchema();
		OutputSchema outputSchema = rolapSchema.getOutputSchema();
		List<Table<?>> tables = outputSchema.getTables();
		for ( Table<?> table : tables ) {
			if (table instanceof OutputDataTable){
				createOutputDataTable((OutputDataTable) table);
			}
		}
	}
	
	private void createOutputDataTable(OutputDataTable outputTable) {
		InputDataTable inputTable = (InputDataTable) outputTable.getSourceTable();
		
		if ( isDebugMode() ) {
			psql()
				.dropTableIfExists(outputTable)
				.execute();
		}
		
		// Copying entire table from input schema
		// TODO replace with select of specific columns
		Select<?> select = psql().selectStarFrom(inputTable);
		
		
		psql()
			.createTable(outputTable)
			.as(select)
			.execute();
		
		// Add primary key constraint and index
		psql()
			.alterTable(outputTable)
			.addPrimaryKey(outputTable.getPrimaryKey())
			.execute();
		
		// Add missing columns for variables not in input schema
		for (Field<?> field : outputTable.fields()) {
			String fieldName = field.getName();
			if ( !inputTable.hasField(fieldName) ) { 
				psql()
					.alterTable(outputTable)
					.addColumn(field)
					.execute();
			}
		}
		
		// Grant access to system user
		psql()
			.grant(Privilege.ALL)
			.on(outputTable)
			.to(getSystemUser())
			.execute();
		
	}
}
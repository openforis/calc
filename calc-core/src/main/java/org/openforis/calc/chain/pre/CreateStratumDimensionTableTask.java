package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import org.jooq.Select;
import org.openforis.calc.engine.Task;
import org.openforis.calc.persistence.postgis.Psql.Privilege;
import org.openforis.calc.rdb.OutputSchema;
import org.openforis.calc.rolap.StratumDimensionTable;

/**
 * Copies the data from the stratum table in calc schema to the _stratum_dim table on the output schema

 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 *
 */
public class CreateStratumDimensionTableTask extends Task {

	@Override
	protected void execute() throws Throwable {
		int workspaceId = getWorkspace().getId();
		OutputSchema outputSchema = getJob().getOutputSchema();
		StratumDimensionTable stratumDimensionTable = outputSchema.getStratumDimensionTable();
		
		Select<?> select = psql()
					.select(STRATUM.ID, STRATUM.STRATUM_NO, STRATUM.CAPTION, STRATUM.DESCRIPTION)
					.from(STRATUM)
					.where(STRATUM.WORKSPACE_ID.eq(workspaceId));

		psql()
			.createTable(stratumDimensionTable)
			.as(select)
			.execute();
		
		// Grant access to system user
		psql()
			.grant(Privilege.ALL)
			.on(stratumDimensionTable)
			.to(getSystemUser())
			.execute();		
	}
}

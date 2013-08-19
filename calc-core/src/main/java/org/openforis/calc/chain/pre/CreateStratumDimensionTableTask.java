package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.persistence.postgis.PsqlBuilder;

/**
 * Copies the data from the stratum table in calc schema to the _stratum_dim table on the output schema
 * @author Alfonso Sanchez-Paus Diaz
 *
 */
public class CreateStratumDimensionTableTask extends Task {

	private static final Object CALC_STRATUM_TABLE = "calc.stratum";
	private static final String WORKSPACE_ID = "workspace_id";

	@Override
	protected void execute() throws Throwable {

		Integer workspaceId = getWorkspace().getId();
		PsqlBuilder select = new PsqlBuilder()
			.select("*")
			.from(CALC_STRATUM_TABLE)
			.where(WORKSPACE_ID+"=?");

		psql()
			.createTable("_stratum_dim")
			.as(select) 
			.execute(workspaceId);
	}
}

package org.openforis.calc.metadata.task;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * 
 * @author G. Miceli
 *
 */
public class UpdateStratumAoisTask extends SqlTask {

	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		int wsId = ws.getId();
		deleteExistingStratumAois(wsId);
		insertStratumAois(wsId);
	}

	private void deleteExistingStratumAois(int wsId) {
		Psql subselect = new Psql()
			.select("id")
			.from("calc.stratum")
			.where("workspace_id = ?");
		
		psql()
			.deleteFrom("calc.stratum_aoi")
			.where("stratum_id in ("+subselect+")")
			.execute(wsId);
	}
	
	private void insertStratumAois(int wsId) {
		psql()
			.insertInto("calc.stratum_aoi", "aoi_id", "stratum_id", "weight")
			.select("ua.aoi_id", "u.stratum_id", "count(*)")
			.from("calc.sampling_unit_aoi ua")
			.innerJoin("calc.sampling_unit u").on("u.id = ua.sampling_unit_id")
			.innerJoin("calc.stratum s").on("s.id = u.stratum_id and s.workspace_id = ?")			
			.groupBy("ua.aoi_id", "u.stratum_id")
			.execute(wsId);
	}
}

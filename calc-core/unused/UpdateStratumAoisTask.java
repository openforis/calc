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
			.insertInto("calc.stratum_aoi", "entity_id", "aoi_id", "stratum_id", "weight")
			.select("u.entity_id", "ua.aoi_id", "u.stratum_id", "count(*)::double precision / tot.count")
			.from("calc.sampling_unit_aoi ua")
			.innerJoin("calc.sampling_unit u").on("u.id = ua.sampling_unit_id")
			.innerJoin("calc.sampling_unit_count_view tot").on("tot.entity_id = u.entity_id and tot.workspace_id = ?")			
			.groupBy("u.entity_id", "ua.aoi_id", "u.stratum_id", "tot.count")
			.execute(wsId);
	}
}

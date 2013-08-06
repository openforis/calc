package org.openforis.calc.metadata.task;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * 
 * @author G. Miceli
 *
 */
public class UpdateStratumWeightsTask extends SqlTask {

	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		int wsId = ws.getId();	
		removeOldWeights(wsId);
		updateWeights(wsId);
	}

	private void removeOldWeights(int wsId) {
		psql()
			.update("calc.stratum s")
			.set("weight = null")
			.where("workspace_id = ?")
			.execute(wsId);
	}

	private void updateWeights(int wsId) {
		Psql select = new Psql()
			.select("stratum_id", "sum(weight)")
			.from("calc.stratum_aoi")
			.groupBy("stratum_id");
			
		psql()
			.with("tot", select)
			.update("calc.stratum s")
			.set("weight = tot.sum")
			.from("tot")
			.where("s.id = tot.stratum_id")
			.and("workspace_id = ?")
			.execute(wsId);
	}
}

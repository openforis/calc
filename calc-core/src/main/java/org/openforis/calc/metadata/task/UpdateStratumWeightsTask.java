package org.openforis.calc.metadata.task;

import org.openforis.calc.engine.SqlTask;

/**
 * 
 * @author G. Miceli
 *
 */
public class UpdateStratumWeightsTask extends SqlTask {

	@Override
	protected void execute() throws Throwable {
		// TODO
//		update calc.stratum s
//		set weight = null
//		where workspace_id = 1;
//
//		with tot as (select stratum_id, sum(weight) from calc.stratum_aoi group by stratum_id)    
//		update calc.stratum s
//		set weight = tot.sum
//		from tot
//		where s.id = tot.stratum_id;

	}
}

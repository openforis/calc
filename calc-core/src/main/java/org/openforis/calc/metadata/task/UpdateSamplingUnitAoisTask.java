package org.openforis.calc.metadata.task;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Task responsible for assigning AOI codes and/or ids to the first phase plots.
 * 
 * @author M. Togna
 */
public final class UpdateSamplingUnitAoisTask extends SqlTask {
	
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		
		Psql select = new Psql().select("1").from("calc.sampling_unit_aoi a").where("a.workspace_id = ?");		
		Boolean exists = psql().selectExists(select).queryForBoolean( ws.getId() );

		if( !exists ) {
			populateSamplingUnitAoiTable(ws);
		}
	}

	private void populateSamplingUnitAoiTable(Workspace ws) {
		List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
		for ( AoiHierarchy hierarchy : hierarchies ) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			
			AoiHierarchyLevel childLevel = null;
			for ( int i = levels.size() - 1 ; i >= 0 ; i-- ) {
				AoiHierarchyLevel level = levels.get(i);
				
				if(childLevel == null) {
					insertLeafStratumAois(ws, level);
				} else {
					insertAncestorStratumAois(ws, childLevel);
				}
				
				childLevel = level;
			}
		}
	}

	private void insertAncestorStratumAois(Workspace ws,
			AoiHierarchyLevel childLevel) {
		psql()
		.insertInto("calc.sampling_unit_aoi","sampling_unit_id", "aoi_id","workspace_id")
		.select("s.sampling_unit_id", "a.parent_aoi_id", ws.getId() )
		.from("calc.sampling_unit_aoi s")
		.innerJoin("calc.aoi a")
		.on("s.aoi_id = a.id")
		.and("a.aoi_level_id = " + childLevel.getId() )
		.execute();
	}

	private void insertLeafStratumAois(Workspace ws, AoiHierarchyLevel level) {
		psql()
			.insertInto("calc.sampling_unit_aoi","sampling_unit_id", "aoi_id","workspace_id")
			.select("u.id", "a.id", ws.getId())
			.from("calc.sampling_unit u")
			.innerJoin("calc.entity e")
			.on("u.entity_id = e.id")
			.and("e.workspace_id  = " + ws.getId())
			.innerJoin("calc.aoi a")
			.on("ST_Contains(a.shape, u.location)")
			.and("a.aoi_level_id = " + level.getId())
			.execute();
	}


}


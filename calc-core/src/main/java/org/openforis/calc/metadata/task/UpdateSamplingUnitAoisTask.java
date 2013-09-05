package org.openforis.calc.metadata.task;

import static org.openforis.calc.persistence.jooq.tables.AoiTable.AOI;
import static org.openforis.calc.persistence.jooq.tables.EntityTable.ENTITY;
import static org.openforis.calc.persistence.jooq.tables.SamplingUnitAoiTable.SAMPLING_UNIT_AOI;
import static org.openforis.calc.persistence.jooq.tables.SamplingUnitTable.SAMPLING_UNIT;

import java.util.List;

import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;

/**
 * Task responsible for assigning AOI codes and/or ids to the first phase plots.
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public final class UpdateSamplingUnitAoisTask extends Task {
	
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		deleteSamplingUnitAois(ws.getId());
		populateSamplingUnitAois(ws);
	}

	private void deleteSamplingUnitAois(int wsId) {
		psql()
			.delete(SAMPLING_UNIT_AOI)
			.where(SAMPLING_UNIT_AOI.WORKSPACE_ID.eq(wsId))
			.execute();
	}

	private void populateSamplingUnitAois(Workspace ws) {
		List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
		for ( AoiHierarchy hierarchy : hierarchies ) {
			List<AoiLevel> levels = hierarchy.getLevels();
			
			AoiLevel childLevel = null;
			for ( int i = levels.size() - 1 ; i >= 0 ; i-- ) {
				AoiLevel level = levels.get(i);
				log().debug("AOI Level: "+level);
				
				if (childLevel == null) {
					insertLeafStratumAois(ws, level);
				} else {
					insertAncestorStratumAois(ws, childLevel);
				}
				
				childLevel = level;
			}
		}
	}

	private void insertAncestorStratumAois(Workspace ws,
			AoiLevel childLevel) {
		Insert<Record> insert = psql()
			.insertInto(SAMPLING_UNIT_AOI, 
					SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID,
					SAMPLING_UNIT_AOI.AOI_ID,
					SAMPLING_UNIT_AOI.WORKSPACE_ID)
			.select(psql()
					.select(
							SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID,
							AOI.PARENT_AOI_ID, 
							DSL.val(ws.getId()))
					.from(SAMPLING_UNIT_AOI)
						.join(AOI)
						.on(AOI.ID.eq(SAMPLING_UNIT_AOI.AOI_ID))
					.where(AOI.AOI_LEVEL_ID.eq(childLevel.getId()))
					);
		insert.execute();
	}

	private void insertLeafStratumAois(Workspace ws, AoiLevel level) {
		Insert<Record> insert = psql()
			.insertInto(SAMPLING_UNIT_AOI, 
					SAMPLING_UNIT_AOI.SAMPLING_UNIT_ID, 
					SAMPLING_UNIT_AOI.AOI_ID, 
					SAMPLING_UNIT_AOI.WORKSPACE_ID)
			.select(psql()
					.select(SAMPLING_UNIT.ID, AOI.ID, DSL.val(ws.getId()))
					.from(SAMPLING_UNIT)
						.join(ENTITY)
							.on(SAMPLING_UNIT.ENTITY_ID.eq(ENTITY.ID))
						.join(AOI).on("ST_Contains("+ AOI.SHAPE +","+ SAMPLING_UNIT.LOCATION+")")
					.where(ENTITY.WORKSPACE_ID.eq(ws.getId())
							.and(AOI.AOI_LEVEL_ID.eq(level.getId()))
					)
				);
		insert.execute();
	}


}


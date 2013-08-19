package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.postgis.Psql.INTEGER;
import static org.openforis.calc.persistence.postgis.Psql.quote;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Task responsible for assigning AOI codes and/or ids to an output table based on a Point column.
 * Assigns AOI ids to each georeferenced entity
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CreateAoiColumnsTask extends Task {
	
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			if (entity.isGeoreferenced()) {
				createAoiColumns(entity);
			}
		}
	}

	private void createAoiColumns(Entity entity) {
		Workspace workspace = entity.getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for ( AoiHierarchy hierarchy : hierarchies ) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			
			AoiHierarchyLevel childLevel = null;
			for ( int i = levels.size() - 1 ; i >= 0 ; i-- ) {
				AoiHierarchyLevel level = levels.get(i);
				
				createAoiColumns(entity, level, childLevel);
				
				childLevel = level;
			}
		}
		
	}

	private void createAoiColumns(Entity entity, AoiHierarchyLevel level, AoiHierarchyLevel childLevel) {
		// add AOI id column to fact table output schema
		String dataTable = quote(entity.getDataTable());
		String aoiFkColumn = quote(level.getFkColumn());
		
		psql()
			.alterTable(dataTable)
			.addColumn(aoiFkColumn, INTEGER)
			.execute();
		
		//update aoi column value 
		String factIdColumn = quote(entity.getIdColumn());
		String aoiDimTable = quote(level.getDimensionTable());
		
		//spatial query only for leaf aoi hierarchy level 
		if( childLevel == null ) {
			Integer levelId = level.getId();
			updateLeafAoi(dataTable, aoiFkColumn, factIdColumn,
					aoiDimTable, levelId);
		} else {
			String childAoiFkColumn = quote(childLevel.getFkColumn());
			String childAoiDimTable = quote(childLevel.getDimensionTable());
			
			updateAncesctorAoi(dataTable, aoiFkColumn, childAoiFkColumn,
					childAoiDimTable);
		}
	}

	private void updateAncesctorAoi(String dataTable, String aoiFkColumn,
			String childAoiFkColumn, String childAoiDimTable) {
		Psql selectAois = new Psql()
			.select("a.id, a.parent_aoi_id")
			.from(childAoiDimTable +" a");
		
		psql()
			.with("tmp", selectAois)
			.update(dataTable+" f")
				.set(aoiFkColumn + " = tmp.parent_aoi_id")
				.from("tmp")
				.where("f."+childAoiFkColumn+"  = tmp.id")
				.execute();
	}

	private void updateLeafAoi(String dataTable, String aoiFkColumn,
			String factIdColumn, String aoiDimTable, Integer levelId) {
		Psql selectAois = new Psql()
			.select("f."+factIdColumn+" as fid", "a.id as aid")
			.from(dataTable+" f")
			.innerJoin(aoiDimTable+" a")
			.on("ST_Contains(a.shape, f."+CreateLocationColumnsTask.LOCATION_COLUMN+")")
			.and("a.aoi_level_id = " + levelId);
		
		psql()
			.with("tmp", selectAois)
			.update(dataTable+" f")
				.set(aoiFkColumn + " = aid")
				.from("tmp")
				.where("f."+factIdColumn+" = tmp.fid")
				.execute();
	}

}



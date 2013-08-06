package org.openforis.calc.chain.pre;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import static org.openforis.calc.persistence.postgis.Psql.*;
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
		
		//select exists (select 1 from sampling_unit_aoi a where a.workspace_id = 1)
//		select 1 from sampling_unit_aoi a where a.workspace_id = 18
		Psql select = new Psql().select("1").from("calc.sampling_unit_aoi a").where("a.workspace_id = ?");		
		Boolean exists = psql().selectExists(select).queryForBoolean( ws.getId() );
		System.out.println(exists);
		if( !exists ) {
			List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
			for ( AoiHierarchy hierarchy : hierarchies ) {
				List<AoiHierarchyLevel> levels = hierarchy.getLevels();
				
				AoiHierarchyLevel childLevel = null;
				for ( int i = levels.size() - 1 ; i >= 0 ; i-- ) {
					AoiHierarchyLevel level = levels.get(i);
					
					if(childLevel == null) {
						//leaf
												
						psql()
						.insertInto("calc.sampling_unit_aoi","sampling_unit_id", "aoi_id","workspace_id")
						.select("u.id", "a.id", ws.getId())
						.from("calc.sampling_unit u")
						.innerJoin("calc.entity e")
						.on("u.entity_id = e.id")
						.and("e.workspace_id  = ?")
						.innerJoin("calc.aoi a")
						.on("ST_Contains(a.shape, u.location)")
						.and("a.aoi_level_id = ?")
						.execute(ws.getId(), level.getId());
						
						
					} else {
						
					}
					
					childLevel = level;
				}
			}
		}
//		List<Entity> entities = ws.getEntities();
//		for (Entity entity : entities) {
//			if (entity.isGeoreferenced()) {
//				createAoiColumns(entity);
//			}
//		}
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
			Psql selectAois = new Psql()
			.select("f."+factIdColumn+" as fid", "a.id as aid")
			.from(dataTable+" f")
			.innerJoin(aoiDimTable+" a")
			.on("ST_Contains(a.shape, f."+CreateLocationColumnsTask.LOCATION_COLUMN+")")
			.and("a.aoi_level_id = " + level.getId());
			
			psql()
			.with("tmp", selectAois)
			.update(dataTable+" f")
				.set(aoiFkColumn, "aid")
				.from("tmp")
				.where("f."+factIdColumn+" = tmp.fid")
				.execute();
		} else {
			String childAoiFkColumn = quote(childLevel.getFkColumn());
			String childAoiDimTable = quote(childLevel.getDimensionTable());
			
			Psql selectAois = new Psql()
			.select("a.id, a.parent_aoi_id")
			.from(childAoiDimTable +" a");
			
			psql()
			.with("tmp", selectAois)
			.update(dataTable+" f")
				.set(aoiFkColumn, "tmp.parent_aoi_id")
				.from("tmp")
				.where("f."+childAoiFkColumn+"  = tmp.id")
				.execute();
		}
	}

}



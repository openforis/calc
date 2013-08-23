package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.postgis.PsqlBuilder.INTEGER;
import static org.openforis.calc.persistence.postgis.PsqlBuilder.quote;

import java.util.Collection;
import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.postgis.PsqlBuilder;
import org.openforis.calc.schema.AoiDimensionTable;
import org.openforis.calc.schema.OutputDataTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Task responsible for assigning AOI codes and/or ids to an output table based on a Point column. Assigns AOI ids to each georeferenced entity
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@SuppressWarnings("deprecation")
public final class AssignAoiColumnsTask extends Task {

	@Override
	protected void execute() throws Throwable {

		OutputSchema outputSchema = getOutputSchema();
		Collection<OutputDataTable> tables = outputSchema.getDataTables();
		for ( OutputDataTable table : tables ) {
			if ( table.getEntity().isGeoreferenced() ) {
				populateAoiColumns(table);
			}
		}

		// Workspace ws = getWorkspace();
		// List<Entity> entities = ws.getEntities();
		// for ( Entity entity : entities ) {
		// if ( entity.isGeoreferenced() ) {
		// createAoiColumns(entity);
		// }
		// }
	}

	private void populateAoiColumns(OutputDataTable table) {
		Workspace workspace = getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();

		for ( AoiHierarchy hierarchy : hierarchies ) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			AoiHierarchyLevel childLevel = null;
			for ( int i = levels.size() - 1 ; i >= 0 ; i-- ) {
				AoiHierarchyLevel level = levels.get(i);

				populateAoiColumn(table, level, childLevel);
				childLevel = level;
			}
		}
	}

	private void populateAoiColumn(OutputDataTable table, AoiHierarchyLevel level, AoiHierarchyLevel childLevel) {
		OutputSchema outputSchema = getOutputSchema();
		AoiDimensionTable aoiDimTable = outputSchema.getAoiDimensionTable(level);
		
		// spatial query only for leaf aoi hierarchy level
		if ( childLevel == null ) {
			
//			new Psql()
//				.select()
//				.from(tables)
//			
//			
//			PsqlBuilder selectAois = 
//					new PsqlBuilder()
//						.select("f." + factIdColumn + " as fid", "a.id as aid")
//						.from(dataTable + " f")
//						.innerJoin(aoiDimTable + " a")
//							.on("ST_Contains(a.shape, f." + CreateLocationColumnsTask.LOCATION_COLUMN + ")")
//							.and("a.aoi_level_id = " + levelId);
//
//			createPsqlBuilder()
//				.with("tmp", selectAois)
//				.update(dataTable + " f")
//				.set(aoiFkColumn + " = aid")
//				.from("tmp")
//				.where("f." + factIdColumn + " = tmp.fid")
//				.execute();

		} else {
			
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

		createPsqlBuilder().alterTable(dataTable).addColumn(aoiFkColumn, INTEGER).execute();

		// update aoi column value
		String factIdColumn = quote(entity.getIdColumn());
		String aoiDimTable = quote(level.getDimensionTable());

		// spatial query only for leaf aoi hierarchy level
		if ( childLevel == null ) {
			Integer levelId = level.getId();
			updateLeafAoi(dataTable, aoiFkColumn, factIdColumn, aoiDimTable, levelId);
		} else {
			String childAoiFkColumn = quote(childLevel.getFkColumn());
			String childAoiDimTable = quote(childLevel.getDimensionTable());

			updateAncesctorAoi(dataTable, aoiFkColumn, childAoiFkColumn, childAoiDimTable);
		}
	}

	private void updateAncesctorAoi(String dataTable, String aoiFkColumn, String childAoiFkColumn, String childAoiDimTable) {
		PsqlBuilder selectAois = new PsqlBuilder().select("a.id, a.parent_aoi_id").from(childAoiDimTable + " a");

		createPsqlBuilder().with("tmp", selectAois).update(dataTable + " f").set(aoiFkColumn + " = tmp.parent_aoi_id").from("tmp").where("f." + childAoiFkColumn + "  = tmp.id").execute();
	}

	private void updateLeafAoi(String dataTable, String aoiFkColumn, String factIdColumn, String aoiDimTable, Integer levelId) {

		PsqlBuilder selectAois = new PsqlBuilder().select("f." + factIdColumn + " as fid", "a.id as aid").from(dataTable + " f").innerJoin(aoiDimTable + " a")
				.on("ST_Contains(a.shape, f." + CreateLocationColumnsTask.LOCATION_COLUMN + ")").and("a.aoi_level_id = " + levelId);

		createPsqlBuilder().with("tmp", selectAois).update(dataTable + " f").set(aoiFkColumn + " = aid").from("tmp").where("f." + factIdColumn + " = tmp.fid").execute();
	}
}

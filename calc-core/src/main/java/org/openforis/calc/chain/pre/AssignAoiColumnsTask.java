package org.openforis.calc.chain.pre;

import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.Update;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.persistence.postgis.Psql;
import org.openforis.calc.persistence.postgis.UpdateWithStep;
import org.openforis.calc.schema.AoiDimensionTable;
import org.openforis.calc.schema.OutputDataTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Task responsible for assigning AOI codes and/or ids to an output table based on a Point column. <br/>
 * Assigns AOI ids to each table associated to a georeferenced entity
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class AssignAoiColumnsTask extends Task {

	@Override
	protected void execute() throws Throwable {

		OutputSchema outputSchema = getOutputSchema();
		Collection<OutputDataTable> tables = outputSchema.getDataTables();
		
		for ( OutputDataTable table : tables ) {
			if ( table.getEntity().isGeoreferenced() ) {
				assignAoiColumns(table);
			}
		}

	}

	private void assignAoiColumns(OutputDataTable dataTable) {
		Workspace workspace = getWorkspace();
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();

		for ( AoiHierarchy hierarchy : hierarchies ) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			
			AoiHierarchyLevel childLevel = null;
			for ( int i = levels.size() - 1 ; i >= 0 ; i-- ) {
				
				AoiHierarchyLevel level = levels.get(i);
				AoiDimensionTable aoiTable = getOutputSchema().getAoiDimensionTable(level);
				
				// spatial query only for leaf aoi hierarchy level
				if ( childLevel == null ) {
					assignLeafAoiColumn(dataTable, aoiTable, level);
				} else {
					assignAncestorAoiColumn(dataTable, childLevel, aoiTable);
				}
				
				childLevel = level;
			}
			
		}
	}

	@SuppressWarnings("unchecked")
	private void assignLeafAoiColumn(OutputDataTable dataTable, AoiDimensionTable aoiTable,  AoiHierarchyLevel level) {
		
		UniqueKey<Record> tablePK = dataTable.getPrimaryKey();
		Field<Integer> dataTablePKField = (Field<Integer>) tablePK.getFields().get(0);
		Field<Integer> dataTableAoiFkField = (Field<Integer>)dataTable.field(level.getFkColumn());
		
		Table<?> cursor = new Psql()
							.select(dataTablePKField, aoiTable.ID)				
							.from(dataTable)
							.join(aoiTable, JoinType.JOIN)
							.on("ST_Contains(" + aoiTable.SHAPE +","+dataTable.field(AssignLocationColumnsTask.LOCATION_COLUMN)+")" )
							.and( aoiTable.AOI_LEVEL_ID.eq(level.getId()) )
							.asTable("tmp");
		
		Update<?> update =  new Psql()
								.update(dataTable)
								.set( dataTableAoiFkField , cursor.field(aoiTable.ID) );
		
		UpdateWithStep updateWithStep =  new Psql().updateWith( cursor, update, dataTablePKField.eq(cursor.field(dataTablePKField)) );
			
		updateWithStep.execute();
	}
	
	@SuppressWarnings("unchecked")
	private void assignAncestorAoiColumn(OutputDataTable dataTable, AoiHierarchyLevel childLevel, AoiDimensionTable aoiTable) {
		AoiDimensionTable childAoiTable = getOutputSchema().getAoiDimensionTable(childLevel); 
		Field<Integer> dataTableChildAoiFkField = (Field<Integer>)dataTable.field(childLevel.getFkColumn());	
		
		Table<?> cursor = new Psql()
								.select( childAoiTable.ID, childAoiTable.PARENT_AOI_ID )				
								.from(childAoiTable)
								.asTable("tmp");
		
		Update<?> update =  new Psql()
								.update(dataTable)
								.set( dataTableChildAoiFkField , cursor.field(aoiTable.PARENT_AOI_ID) );
		
		UpdateWithStep updateWithStep = new Psql().updateWith( cursor, update, dataTableChildAoiFkField.eq( cursor.field(childAoiTable.ID) ) );
		
		updateWithStep.execute();
	}

//	private void createAoiColumns(Entity entity, AoiHierarchyLevel level, AoiHierarchyLevel childLevel) {
//		// add AOI id column to fact table output schema
//		String dataTable = quote(entity.getDataTable());
//		String aoiFkColumn = quote(level.getFkColumn());
//
//		createPsqlBuilder().alterTable(dataTable).addColumn(aoiFkColumn, INTEGER).execute();
//
//		// update aoi column value
//		String factIdColumn = quote(entity.getIdColumn());
//		String aoiDimTable = quote(level.getDimensionTable());
//
//		// spatial query only for leaf aoi hierarchy level
//		if ( childLevel == null ) {
//			Integer levelId = level.getId();
//			updateLeafAoi(dataTable, aoiFkColumn, factIdColumn, aoiDimTable, levelId);
//		} else {
//			String childAoiFkColumn = quote(childLevel.getFkColumn());
//			String childAoiDimTable = quote(childLevel.getDimensionTable());
//
//			updateAncesctorAoi(dataTable, aoiFkColumn, childAoiFkColumn, childAoiDimTable);
//		}
//	}

//	private void updateAncesctorAoi(String dataTable, String aoiFkColumn, String childAoiFkColumn, String childAoiDimTable) {
//		PsqlBuilder selectAois = new PsqlBuilder().select("a.id, a.parent_aoi_id").from(childAoiDimTable + " a");
//
//		createPsqlBuilder().with("tmp", selectAois).update(dataTable + " f").set(aoiFkColumn + " = tmp.parent_aoi_id").from("tmp").where("f." + childAoiFkColumn + "  = tmp.id").execute();
//	}
//
//	private void updateLeafAoi(String dataTable, String aoiFkColumn, String factIdColumn, String aoiDimTable, Integer levelId) {
//
//		PsqlBuilder selectAois = new PsqlBuilder().select("f." + factIdColumn + " as fid", "a.id as aid").from(dataTable + " f").innerJoin(aoiDimTable + " a")
//				.on("ST_Contains(a.shape, f." + CreateLocationColumnsTask.LOCATION_COLUMN + ")").and("a.aoi_level_id = " + levelId);
//
//		createPsqlBuilder().with("tmp", selectAois).update(dataTable + " f").set(aoiFkColumn + " = aid").from("tmp").where("f." + factIdColumn + " = tmp.fid").execute();
//	}
}

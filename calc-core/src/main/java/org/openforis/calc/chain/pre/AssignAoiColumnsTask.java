package org.openforis.calc.chain.pre;

import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.schema.AoiHierarchyFlatTable;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.InputSchema;

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

		Workspace workspace = getWorkspace();
		InputSchema schema = getInputSchema();

		// only first hierarchy at the moment.
		List<AoiHierarchyFlatTable> hierchyTables = schema.getAoiHierchyTables();
		AoiHierarchyFlatTable hierarchyTable = hierchyTables.get(0);
		
		SamplingDesign samplingDesign = workspace.getSamplingDesign();
		
		DataAoiTable dataAoiTable = null;
		if( samplingDesign.getTwoPhases() ) {
			dataAoiTable = schema.getPhase1AoiTable();
		} else {
			dataAoiTable = schema.getSamplingUnitAoiTable();
		}
		createAoiJoinTable( dataAoiTable , hierarchyTable , samplingDesign.getAoiJoin() );

	}

	private void createAoiJoinTable(DataAoiTable dataAoiTable, AoiHierarchyFlatTable hierarchyTable, ColumnJoin columnJoin) {
		
		// drop table first
		psql()
			.dropTableIfExists( dataAoiTable )
			.execute();
		
		// create table 
		DynamicTable<?> dataTable = new DynamicTable<Record>( columnJoin.getTable(), columnJoin.getSchema() );
			
		SelectQuery<Record> select = hierarchyTable.getSelectQuery();
		
		Field<String> joinField = dataTable.getVarcharField( columnJoin.getColumn() );
		String aliasJoin = hierarchyTable.getAoiHierarchy().getLeafLevel().getNormalizedName();
		select.addJoin( dataTable ,	Tables.AOI.as( aliasJoin ).CODE.eq(joinField) );
		select.addSelect( dataTable.getIntegerField("id") );
			
		psql()
			.createTable( dataAoiTable )
			.as( select )
			.execute() ;
			
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@Override
//	protected void executeOld() throws Throwable {
//
//		OutputSchema outputSchema = getOutputSchema();
//		Collection<OutputTable> tables = outputSchema.getOutputTables();
//		
//		for ( OutputTable table : tables ) {
//			if ( table.getEntity().isGeoreferenced() ) {
//				assignAoiColumnsOld(table);
//			}
//		}
//
//	}
//
//	private void assignAoiColumnsOld(OutputTable dataTable) {
//		Workspace workspace = getWorkspace();
//		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
//
//		for ( AoiHierarchy hierarchy : hierarchies ) {
//			Set<AoiLevel> levels = hierarchy.getLevels();
//			Iterator<AoiLevel> iterator = new LinkedList<AoiLevel>(levels).descendingIterator();
//			AoiLevel childLevel = null;
//			while(iterator.hasNext()){
////			for ( int i = levels.size() - 1 ; i >= 0 ; i-- ) {
//				
////				AoiLevel level = levels.get(i);
//				AoiLevel level = iterator.next();
//				AoiDimensionTable aoiTable = getOutputSchema().getAoiDimensionTable(level);
//				
//				// spatial query only for leaf aoi hierarchy level
//				if ( childLevel == null ) {
//					assignLeafAoiColumnOld(dataTable, aoiTable, level);
//				} else {
//					assignAncestorAoiColumnOld(dataTable, aoiTable, level, childLevel);
//				}
//				
//				childLevel = level;
//			}
//			
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	private void assignLeafAoiColumnOld(OutputTable dataTable, AoiDimensionTable aoiTable,  AoiLevel level) {
//		
//		UniqueKey<Record> tablePK = dataTable.getPrimaryKey();
//		Field<Integer> dataTablePKField = (Field<Integer>) tablePK.getFields().get(0);
//		Field<Integer> dataTableAoiFkField = (Field<Integer>)dataTable.field(level.getFkColumn());
//		
//		Table<?> cursor = new Psql()
//							.select(dataTablePKField, aoiTable.ID)				
//							.from(dataTable)
//							.join(aoiTable, JoinType.JOIN)
//							.on("ST_Contains(" + aoiTable.SHAPE +","+dataTable.field(AssignLocationColumnsTask.LOCATION_COLUMN)+")" )
//							.and( aoiTable.AOI_LEVEL_ID.eq(level.getId()) )
//							.asTable("tmp");
//		
//		Update<?> update =  new Psql()
//								.update(dataTable)
//								.set( dataTableAoiFkField , cursor.field(aoiTable.ID) );
//		
//		UpdateWithStep updateWithStep =  psql().updateWith( cursor, update, dataTablePKField.eq(cursor.field(dataTablePKField)) );
//			
//		updateWithStep.execute();
//	}
//	
//	@SuppressWarnings("unchecked")
//	private void assignAncestorAoiColumnOld(OutputTable dataTable, AoiDimensionTable aoiTable, AoiLevel level, AoiLevel childLevel) {
//		AoiDimensionTable childAoiTable = getOutputSchema().getAoiDimensionTable(childLevel); 
//		Field<Integer> dataTableChildAoiFkField = (Field<Integer>)dataTable.field(childLevel.getFkColumn());	
//		Field<Integer> dataTableAoiFkField = (Field<Integer>)dataTable.field(level.getFkColumn());
//		
//		Table<?> cursor = new Psql()
//								.select( childAoiTable.ID, childAoiTable.PARENT_AOI_ID )				
//								.from(childAoiTable)
//								.asTable("tmp");
//		
//		Update<?> update =  new Psql()
//								.update(dataTable)
//								.set( dataTableAoiFkField , cursor.field(aoiTable.PARENT_AOI_ID) );
//		
//		UpdateWithStep updateWithStep = psql().updateWith( cursor, update, dataTableChildAoiFkField.eq( cursor.field(childAoiTable.ID) ) );
//		
//		updateWithStep.execute();
//	}

	@Override
	public String getName() {
		return "Assign area of interest columns";
	}
	
}

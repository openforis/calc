package org.openforis.calc.chain.pre;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.metadata.SamplingDesign.TableJoin;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.schema.AoiHierarchyFlatTable;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.Phase1AoiTable;

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
		DataSchema schema = getInputSchema();

		// only first hierarchy at the moment.
		List<AoiHierarchyFlatTable> hierchyTables = schema.getAoiHierchyTables();
		AoiHierarchyFlatTable hierarchyTable = hierchyTables.get(0);
		
		SamplingDesign samplingDesign = workspace.getSamplingDesign();
		
//		DataAoiTable dataAoiTable = null;
		if( samplingDesign.getTwoPhases() ) {
//			dataAoiTable = schema.getPhase1AoiTable();			
			createAoiJoinTable2Phases( schema.getPhase1AoiTable() , hierarchyTable , samplingDesign.getAoiJoin() );
			createSamplingUnitAoi();
			
		} else {
			Entity samplingUnit = samplingDesign.getSamplingUnit();
			EntityDataView suView = schema.getDataView( samplingUnit );
			createAoiJoinTable1Phase( schema.getSamplingUnitAoiTable() , hierarchyTable , suView , samplingDesign.getAoiJoin() );
//			dataAoiTable = schema.getSamplingUnitAoiTable();
		}

	}

//	@SuppressWarnings("unchecked")
	private void createSamplingUnitAoi() {
		DynamicTable<Record> phase1Table = getInputSchema().getPhase1Table();// new DynamicTable<Record>( getWorkspace().getPhase1PlotTable(), "calc" );
		Phase1AoiTable phase1AoiTable = getInputSchema().getPhase1AoiTable();
		EntityDataView suDataView = getInputSchema().getDataView( getWorkspace().getSamplingUnit() );
		
		DataAoiTable suAoiTable = getInputSchema().getSamplingUnitAoiTable();
		
		
		SelectQuery<Record> select = psql().selectQuery();
		select.setDistinct(true);
		select.addSelect( suDataView.getIdField().as(suAoiTable.getIdField().getName()) );
		select.addFrom( suDataView );
		
		// join with phase 1 table to select id
		TableJoin phase1Join = getWorkspace().getSamplingDesign().getPhase1Join();
		Condition conditions = phase1Table.getJoinConditions(suDataView, phase1Join);
//		for ( int i =0; i < phase1Join.getColumnJoinSize(); i++ ) {
//			ColumnJoin leftColumn = phase1Join.getLeft().getColumnJoins().get(i);
//			ColumnJoin rightJoin = phase1Join.getRight().getColumnJoins().get(i);
//			Field<String> leftField = phase1Table.getVarcharField(leftColumn.getColumn());				
//			Field<String> rightField = (Field<String>)suDataView.field(rightJoin.getColumn());
//			
//			if( conditions == null ) {
//				conditions = leftField.eq( rightField );
//			} else {
//				conditions = conditions.and( leftField.eq( rightField) );
//			}
//		}
		select.addJoin(phase1Table, conditions);
		
		// now join with phase1aoi table to select the aoi ids
		select.addSelect( phase1AoiTable.getAoiIdFields() );
		select.addSelect( phase1AoiTable.getAoiAreaFields() );
		select.addSelect( phase1AoiTable.getAoiCodeFields() );
		select.addSelect( phase1AoiTable.getAoiCaptionFields() );
		select.addJoin(phase1AoiTable, phase1Table.getIdField().eq(phase1AoiTable.getIdField()) );

		
		psql()
			.dropTableIfExists( suAoiTable )
			.execute();
		
		// create table
		psql()
			.createTable(suAoiTable)
			.as(select)
			.execute();
	}

	private void createAoiJoinTable2Phases(DataAoiTable dataAoiTable, AoiHierarchyFlatTable hierarchyTable, ColumnJoin columnJoin) {
		
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

	private void createAoiJoinTable1Phase(DataAoiTable dataAoiTable, AoiHierarchyFlatTable hierarchyTable, EntityDataView samplingUnitView, ColumnJoin columnJoin) {
		
		// drop table first
		psql()
			.dropTableIfExists( dataAoiTable )
			.execute();
		
		// create table 
			
		SelectQuery<Record> select = hierarchyTable.getSelectQuery();
		
		@SuppressWarnings( "unchecked" )
		Field<String> joinField = (Field<String>) samplingUnitView.field( columnJoin.getColumn() );
		String aliasJoin = hierarchyTable.getAoiHierarchy().getLeafLevel().getNormalizedName();
		select.addJoin( samplingUnitView ,	Tables.AOI.as( aliasJoin ).CODE.eq(joinField) );
		select.addSelect( samplingUnitView.getIdField().as("id") );
			
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

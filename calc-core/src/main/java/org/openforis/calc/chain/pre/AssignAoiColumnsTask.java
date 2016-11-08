package org.openforis.calc.chain.pre;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DynamicTable;
import org.jooq.impl.PrimarySamplingUnitTable;
import org.jooq.impl.SQLDataType;
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
import org.openforis.calc.schema.ExtDataAoiTable;

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
		} else if ( samplingDesign.getTwoStages() ){
			createAoiJoinTable2Stages( schema.getPrimarySUAoiTable() , hierarchyTable , samplingDesign );
		} else {
			Entity samplingUnit = samplingDesign.getSamplingUnit();
			EntityDataView suView = schema.getDataView( samplingUnit );
			createAoiJoinTable1Phase( schema.getSamplingUnitAoiTable() , hierarchyTable , suView , samplingDesign.getAoiJoin() );
//			dataAoiTable = schema.getSamplingUnitAoiTable();
		}

	}

//	@SuppressWarnings("unchecked")
	private void createSamplingUnitAoi() {
		DynamicTable<Record> phase1Table = getInputSchema().getPhase1Table();
		ExtDataAoiTable phase1AoiTable = getInputSchema().getPhase1AoiTable();
		
		EntityDataView suDataView = getInputSchema().getDataView( getWorkspace().getSamplingUnit() );
		
		DataAoiTable suAoiTable = getInputSchema().getSamplingUnitAoiTable();
		
		
		SelectQuery<Record> select = psql().selectQuery();
		select.setDistinct(true);
		select.addSelect( suDataView.getIdField().as(suAoiTable.getIdField().getName()) );
		select.addFrom( suDataView );
		
		// join with phase 1 table to select id
		TableJoin phase1Join = getWorkspace().getSamplingDesign().getPhase1Join();
		Condition conditions = phase1Table.getJoinConditions(suDataView, phase1Join);
		select.addJoin(phase1Table, conditions);
		
		// now join with phase1aoi table to select the aoi ids
		select.addSelect( phase1AoiTable.getAoiIdFields() );
		select.addSelect( phase1AoiTable.getAoiAreaFields() );
		select.addSelect( phase1AoiTable.getAoiCodeFields() );
		select.addSelect( phase1AoiTable.getAoiCaptionFields() );
		select.addJoin(phase1AoiTable, phase1Table.getIdField().eq(phase1AoiTable.getIdField()) );

		
		psql()
			.dropTableIfExistsLegacy( suAoiTable )
			.execute();
		
		// create table
		psql()
			.createTableLegacy(suAoiTable)
			.as(select)
			.execute();
	}

	private void createAoiJoinTable2Phases(DataAoiTable dataAoiTable, AoiHierarchyFlatTable hierarchyTable, ColumnJoin columnJoin ) {
		
		// drop table first
		psql()
			.dropTableIfExistsLegacy( dataAoiTable )
			.execute();
		
		// create table 
		DynamicTable<?> dataTable = new DynamicTable<Record>( columnJoin.getTable(), columnJoin.getSchema() );
			
		SelectQuery<Record> select = hierarchyTable.getSelectQuery();
		
		Field<String> joinField = dataTable.getVarcharField( columnJoin.getColumn() );
		String aliasJoin = hierarchyTable.getAoiHierarchy().getLeafLevel().getNormalizedName();
		select.addJoin( dataTable ,	Tables.AOI.as( aliasJoin ).CODE.eq(joinField) );

		select.addSelect( dataTable.getIntegerField("id") );

		psql()
			.createTableLegacy( dataAoiTable )
			.as( select )
			.execute() ;
			
	}
	
private void createAoiJoinTable2Stages(DataAoiTable dataAoiTable, AoiHierarchyFlatTable hierarchyTable, SamplingDesign samplingDesign ) {
		ColumnJoin aoiJoin = samplingDesign.getAoiJoin();
		// drop table first
		psql()
			.dropTableIfExistsLegacy( dataAoiTable )
			.execute();
		
		// create table 
//		DynamicTable<?> dataTable = new DynamicTable<Record>( aoiJoin.getTable(), aoiJoin.getSchema() );
		PrimarySamplingUnitTable<?> psuTable = samplingDesign.getPrimarySamplingUnitTable();
		SelectQuery<Record> select = hierarchyTable.getSelectQuery();
		
		Field<String> joinField = psuTable.getVarcharField( aoiJoin.getColumn() );
		String aliasJoin = hierarchyTable.getAoiHierarchy().getLeafLevel().getNormalizedName();
		select.addJoin( psuTable ,	Tables.AOI.as( aliasJoin ).CODE.eq(joinField) );

		select.addSelect( psuTable.getPsuFields() );

		psql()
			.createTableLegacy( dataAoiTable )
			.as( select )
			.execute() ;
			
	}

	private void createAoiJoinTable1Phase(DataAoiTable dataAoiTable, AoiHierarchyFlatTable hierarchyTable, EntityDataView samplingUnitView, ColumnJoin columnJoin) {
		
		// drop table first
		psql()
			.dropTableIfExistsLegacy( dataAoiTable )
			.execute();
		
		// create table 
			
		SelectQuery<Record> select = hierarchyTable.getSelectQuery();
		
		Field<String> joinField = samplingUnitView.field( columnJoin.getColumn() ).cast( SQLDataType.VARCHAR );
		String aliasJoin = hierarchyTable.getAoiHierarchy().getLeafLevel().getNormalizedName();
		select.addJoin( samplingUnitView ,	Tables.AOI.as( aliasJoin ).CODE.eq(joinField) );
		select.addSelect( samplingUnitView.getIdField().as("id") );
			
		psql()
			.createTableLegacy( dataAoiTable )
			.as( select )
			.execute() ;
			
	}
	
	@Override
	public String getName() {
		return "Assign area of interest columns";
	}
	
}

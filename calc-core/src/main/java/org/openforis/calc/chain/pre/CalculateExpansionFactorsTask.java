package org.openforis.calc.chain.pre;

import java.math.BigDecimal;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.Update;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.metadata.SamplingDesign.TableJoin;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.AoiLevelTable;
import org.openforis.calc.persistence.jooq.tables.AoiTable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.Phase1AoiTable;

/**
 * Task responsible for calculating the expansion factor for each stratum in all AOI levels. Results will be stored in a table called _expf in the output schema
 * 
 * @author M. Togna
 */
public final class CalculateExpansionFactorsTask extends Task {

	// public static final String EXPF_TABLE = "_expf";

	@Override
	protected long countTotalItems() {
		AoiHierarchy hierarchy = getWorkspace().getAoiHierarchies().get(0);
		return hierarchy.getLevels().size();
	}
	
	@Override
	protected void execute() throws Throwable {

		Workspace workspace = getWorkspace();
		DataSchema schema = getInputSchema();
		SamplingDesign samplingDesign = getSamplingDesign();

		// only one for now
		AoiHierarchy hierarchy = workspace.getAoiHierarchies().get(0);
		for ( AoiLevel aoiLevel : hierarchy.getLevels() ) {
			ExpansionFactorTable expf = schema.getExpansionFactorTable( aoiLevel );

			// create expf table
			if (samplingDesign.getTwoPhases()) {
				createExpfTable2phases( expf );
			} else {
				createExpfTable1phase( expf);
			}
			
			addWeightField( expf );
			addExpfField( expf );
			
			incrementItemsProcessed();
		}

	}


	private void addExpfField(ExpansionFactorTable expf) {
		psql()
			.alterTable( expf )
			.addColumn( expf.EXPF )
			.execute();
		
		psql()
			.update( expf )
			.set( expf.EXPF, 
					DSL.decode()
						.when( expf.WEIGHT.gt( BigDecimal.ZERO ), expf.AREA.div(expf.WEIGHT) )
						.otherwise(  BigDecimal.ZERO )
						
					 )
			.execute();
	}


	@SuppressWarnings("unchecked")
	private void addWeightField( ExpansionFactorTable expf ) {
		Entity samplingUnit = getWorkspace().getSamplingUnit();
		EntityDataView samplingUnitView = getInputSchema().getDataView(samplingUnit);
		
		
		AoiLevel aoiLevel = expf.getAoiLevel();
		
		SelectQuery<?> selectWeight = psql().selectQuery();
				
		selectWeight.addFrom( samplingUnitView );
		Field<BigDecimal> cntField = samplingUnitView.getWeightField().sum().as( "cnt" );
		selectWeight.addSelect( cntField );
		
		DataAoiTable dataAoiTable = null;
		Field<Long> dataIdField;
		Field<Integer> stratumField = null;
		
		if( getSamplingDesign().getTwoPhases() ) {
			DynamicTable<Record> phase1Table = new DynamicTable<Record>( getWorkspace().getPhase1PlotTable(), "calc" );
			
			dataAoiTable = getInputSchema().getPhase1AoiTable();
			dataIdField = phase1Table.getIdField();
			
			// join with phase 1 table
			TableJoin phase1Join = getSamplingDesign().getPhase1Join();
			Condition conditions = null;
			for ( int i =0; i < phase1Join.getColumnJoinSize(); i++ ) {
				ColumnJoin leftColumn = phase1Join.getLeft().getColumnJoins().get(i);
				ColumnJoin rightJoin = phase1Join.getRight().getColumnJoins().get(i);
				Field<String> leftField = phase1Table.getVarcharField(leftColumn.getColumn());				
				Field<String> rightField = (Field<String>)samplingUnitView.field(rightJoin.getColumn());
				
				Condition joinCondition = leftField.eq( rightField );
				if( conditions == null ) {
					conditions = joinCondition;
				} else {
					conditions = conditions.and( joinCondition );
				}
			}
			selectWeight.addJoin(phase1Table, JoinType.RIGHT_OUTER_JOIN, conditions);
			selectWeight.setDistinct(true);
			
			if( getSamplingDesign().getStratified() ){
				stratumField = phase1Table.getIntegerField( getSamplingDesign().getStratumJoin().getColumn() );
			}
			
		} else {
			dataAoiTable = getInputSchema().getSamplingUnitAoiTable();
			dataIdField = samplingUnitView.getIdField();
			
			if( getSamplingDesign().getStratified() ){
				stratumField = (Field<Integer>) samplingUnitView.field( getSamplingDesign().getStratumJoin().getColumn() );
			}
		}
		
		// join with aoi 
		Field<Long> aoiIdField = dataAoiTable.getAoiIdField(aoiLevel);
		selectWeight.addSelect( aoiIdField );
		selectWeight.addJoin(dataAoiTable, dataAoiTable.getIdField().eq(dataIdField) );
		selectWeight.addGroupBy( aoiIdField );
		
		
		if( stratumField != null ){
			selectWeight.addSelect( stratumField );
			selectWeight.addGroupBy( stratumField );
		} 
		
		// add weight column
		psql()
			.alterTable( expf )
			.addColumn( expf.WEIGHT )
			.execute();
		
		// update weight
		Table<?> cursor = selectWeight.asTable("tmp");
		//cursor.field( expf.AOI_ID. );
		Update<?> update = psql()
			.update( expf )
			.set( expf.WEIGHT, (Field<BigDecimal>)cursor.field( cntField.getName() ) );
		
		Condition joinCondition = expf.AOI_ID.eq( (Field<Integer>)cursor.field(expf.AOI_ID.getName()) );
		if( getSamplingDesign().getStratified() ){
			joinCondition = joinCondition.and( expf.STRATUM.eq( (Field<Integer>)cursor.field(stratumField.getName())) );
		}
		
		// execute update
		UpdateWithStep updateWith = psql().updateWith( cursor, update, joinCondition );
		updateWith.execute();
			
	}

	@SuppressWarnings("unchecked")
	private void createExpfTable1phase( ExpansionFactorTable expf ){
//		DynamicTable<Record> phase1Table = new DynamicTable<Record>( getWorkspace().getPhase1PlotTable(), "calc" );
//		Phase1AoiTable phase1AoiTable = getInputSchema().getPhase1AoiTable();
		DataAoiTable dataAoiTable = getInputSchema().getSamplingUnitAoiTable();
		EntityDataView dataView = getInputSchema().getDataView( getSamplingDesign().getSamplingUnit() );
		
		AoiLevel aoiLevel = expf.getAoiLevel();
		boolean stratified = getWorkspace().hasStratifiedSamplingDesign();
		
		Field<Long> aoiIdField = dataAoiTable.getAoiIdField(aoiLevel);
		Field<BigDecimal> aoiAreaField = dataAoiTable.getAoiAreaField(aoiLevel);
		
		AoiTable a = Tables.AOI;
		AoiLevelTable l = Tables.AOI_LEVEL;
		
		// select totals for aoi
		SelectQuery<Record> selectTotals = psql().selectQuery();
		
		selectTotals.addSelect( dataView.getIdField().count() );
		selectTotals.addSelect( a.ID.as(aoiIdField.getName()) );
		
		selectTotals.addFrom( a );
		selectTotals.addJoin( l , 
								a.AOI_LEVEL_ID.eq(l.ID)
								.and( l.ID.eq(aoiLevel.getId())) );
		
		selectTotals.addJoin( 
				dataAoiTable , 
				JoinType.LEFT_OUTER_JOIN , 
				a.ID.cast(Long.class).eq(aoiIdField) );
		
		selectTotals.addJoin( 
				dataView , 
				JoinType.LEFT_OUTER_JOIN , 
				dataView.getIdField().eq( dataAoiTable.getIdField() ) );
		
		selectTotals.addGroupBy( a.ID );
		
//		Select<?> selectTotals1 = psql()
//			.select( dataView.getIdField().count() , aoiIdField )
//			.from( dataView )
//			.join( dataAoiTable )
//			.on( dataView.getIdField().eq( dataAoiTable.getIdField() ) )
//			.groupBy( aoiIdField );
		
		Table<?> totals = selectTotals.asTable( "totals" );
//		
		// select proportions
		SelectQuery<Record> select = psql().selectQuery();
		
		select.addSelect( dataView.getIdField().count() );
		select.addFrom( dataView );
		
		// join with aoi_table to calculate proportions. if no stratification is applied, then results will be the same
		select.addSelect( totals.field(aoiIdField.getName())  );
//		select.addSelect( dataAoiTable.getAoiAreaField(aoiLevel).as(expf.AREA.getName()) );
		select.addJoin( dataAoiTable, dataView.getIdField().eq(dataAoiTable.getIdField()) );
		select.addGroupBy( totals.field(aoiIdField.getName()) );
		
		if( stratified ) {
			ColumnJoin stratumJoin = getSamplingDesign().getStratumJoin();
			
			Field<Integer> stratumField = (Field<Integer>) dataView.field(stratumJoin.getColumn());
			select.addSelect( stratumField.as(expf.STRATUM.getName()) );
			select.addGroupBy( stratumField );
		}
		
		// join with totals inner query
		Field<?> totalField = totals.field("count");
		select.addSelect( totalField.as("total") );
		select.addJoin(totals , JoinType.RIGHT_OUTER_JOIN , aoiIdField.eq( (Field<Long>)totals.field(aoiIdField.getName())) );
		select.addGroupBy( totalField );
		
		Field<BigDecimal> countField = DSL.cast(totalField, Psql.DOUBLE_PRECISION);
		Field<Integer> proportion = DSL.decode()
			.when( countField.gt(BigDecimal.ZERO), dataView.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ) )
			.otherwise(0)
			.as( expf.PROPORTION.getName() );		
		select.addSelect( proportion );
		
		Field<Integer> area = DSL.decode()
				.when( countField.gt(BigDecimal.ZERO), dataView.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).mul(aoiAreaField) )
				.otherwise(0)
				.as( expf.AREA.getName() );
		select.addSelect( area );
		
		select.addGroupBy( aoiAreaField );
//		( count(p.id) / total.count::double precision ) as proportion,
//        ( count(p.id) / total.count::double precision ) * a._administrative_unit_level_1_area as area
		
		psql()
			.dropTableIfExists( expf )
			.execute();
		
		psql()
			.createTable( expf )
			.as( select )
			.execute();
			
		
	}
	
	@SuppressWarnings("unchecked")
	private void createExpfTable2phases( ExpansionFactorTable expf ){
		DynamicTable<Record> phase1Table = new DynamicTable<Record>( getWorkspace().getPhase1PlotTable(), "calc" );
		Phase1AoiTable phase1AoiTable = getInputSchema().getPhase1AoiTable();
		AoiLevel aoiLevel = expf.getAoiLevel();
		boolean stratified = getWorkspace().hasStratifiedSamplingDesign();
		
		Field<Long> aoiIdField = phase1AoiTable.getAoiIdField(aoiLevel);
		Field<BigDecimal> aoiAreaField = phase1AoiTable.getAoiAreaField(aoiLevel);
		
		
		// select totals for aoi
		Select<?> selectTotals = psql()
			.select( phase1Table.getIdField().count() , aoiIdField )
			.from( phase1Table )
			.join( phase1AoiTable )
			.on( phase1Table.getIdField().eq( phase1AoiTable.getIdField() ) )
			.groupBy( aoiIdField );
		Table<?> totals = selectTotals.asTable( "totals" );
		
		// select proportions
		SelectQuery<Record> select = psql().selectQuery();
		select.addSelect( phase1Table.getIdField().count() );
		select.addFrom( phase1Table );
		
		// join with phase1_aoi_table to calculate proportions. if no stratification is applied, then results will be the same
		select.addSelect( aoiIdField );
		select.addJoin( phase1AoiTable, phase1Table.getIdField().eq(phase1AoiTable.getIdField()) );
		select.addGroupBy( aoiIdField );
		
		if( stratified ){
			ColumnJoin stratumJoin = getSamplingDesign().getStratumJoin();
			
			Field<Integer> stratumField = phase1Table.getIntegerField(stratumJoin.getColumn());
			select.addSelect( stratumField.as(expf.STRATUM.getName()) );
			select.addGroupBy( stratumField );
		}
		
		// join with totals inner query
		Field<?> totalField = totals.field("count");
		select.addSelect( totalField.as("total") );
		select.addJoin(totals, aoiIdField.eq( (Field<Long>)totals.field(aoiIdField.getName())) );
		select.addGroupBy( totalField );
		
		select.addSelect( phase1Table.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).as(expf.PROPORTION.getName()) );
		select.addSelect( phase1Table.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).mul(aoiAreaField).as(expf.AREA.getName()) );
		select.addGroupBy( aoiAreaField );
//		( count(p.id) / total.count::double precision ) as proportion,
//        ( count(p.id) / total.count::double precision ) * a._administrative_unit_level_1_area as area
		
		psql()
			.dropTableIfExists(expf)
			.execute();
		
		psql()
			.createTable(expf)
			.as( select )
			.execute();
			
		
	}
	
	@Override
	public String getName() {
		return "Calculate expansion factor";
	}
	
	protected SamplingDesign getSamplingDesign() {
		SamplingDesign samplingDesign = getWorkspace().getSamplingDesign();
		return samplingDesign;
	}
	
}

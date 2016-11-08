package org.openforis.calc.chain.export;

import java.math.BigDecimal;
import java.util.Collection;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.Update;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.jooq.impl.PrimarySamplingUnitTable;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.metadata.SamplingDesign.TableJoin;
import org.openforis.calc.metadata.SamplingDesign.TwoStagesSettings;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.AoiLevelTable;
import org.openforis.calc.persistence.jooq.tables.AoiTable;
import org.openforis.calc.persistence.jooq.tables.StratumAoiTable;
import org.openforis.calc.persistence.jooq.tables.StratumTable;
import org.openforis.calc.psql.AlterTableStep.AddColumnStep;
import org.openforis.calc.psql.CreateTableStep.AsStep;
import org.openforis.calc.psql.DropTableStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.calc.r.DbSendQuery;
import org.openforis.calc.r.RScript;
import org.openforis.calc.schema.ClusterCountsTable;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.ExtDataAoiTable;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class CalculateExpansionFactorROutputScript extends ROutputScript {

	public CalculateExpansionFactorROutputScript( int index, Workspace  workspace , Schemas schemas ) {
		super( "calculate-expansion-factor.R", createScript(workspace , schemas), Type.SYSTEM, index );
	}

	private static RScript createScript(Workspace workspace, Schemas schemas) {
		RScript r 						= r();
		
		if( workspace.hasSamplingDesign() ){
			
			DataSchema schema 				= schemas.getDataSchema();
			SamplingDesign samplingDesign 	= workspace.getSamplingDesign();
			
			if(workspace.hasClusterSamplingDesign() ){
				RScript createClusterCounts = createClusterCountsTable(workspace, schema);
				r.addScript( createClusterCounts  );
			}
			
			// only one for now
			AoiHierarchy hierarchy = workspace.getAoiHierarchies().get(0);
			for (AoiLevel aoiLevel : hierarchy.getLevels()) {
				
				ExpansionFactorTable expf = schema.getExpansionFactorTable(aoiLevel);
				
				// create expf table
				RScript createExpfTable = null;
				if (samplingDesign.getTwoPhases()) {
					createExpfTable = createExpfTable2phases( workspace, schema , expf);
				} else if (samplingDesign.getTwoStages()) {
					createExpfTable = createExpfTable2stages( workspace, schema , expf);
				} else {
					createExpfTable = createExpfTable1phase( workspace, schema , expf );
				}
				
				r.addScript( createExpfTable );
				
				RScript addWeight 		= addWeightField( workspace, schema , expf );
				RScript addExpfField 	= addExpfField( workspace, expf );
				r.addScript( addWeight );
				r.addScript( addExpfField );
			}
			
		}
		
		return r;
	}

	@SuppressWarnings("unchecked")
	private static RScript createExpfTable2phases( Workspace workspace , DataSchema schema, ExpansionFactorTable expf ){
		RScript r 					= r();
		
		SamplingDesign samplingDesign 		= workspace.getSamplingDesign();
		DynamicTable<Record> phase1Table 	= new DynamicTable<Record>( workspace.getPhase1PlotTable(), "calc" );
		ExtDataAoiTable phase1AoiTable 		= schema.getPhase1AoiTable();
		AoiLevel aoiLevel 					= expf.getAoiLevel();
		boolean stratified 					= workspace.hasStratifiedSamplingDesign();

		Field<Long> aoiIdField 				= phase1AoiTable.getAoiIdField(aoiLevel);
		Field<BigDecimal> aoiAreaField 		= phase1AoiTable.getAoiAreaField(aoiLevel);

		// select totals for aoi
		Select<?> selectTotals = psql()
				.select( phase1Table.getIdField().count(), aoiIdField )
				.from( phase1Table ).join( phase1AoiTable )
				.on( phase1Table.getIdField().eq( phase1AoiTable.getIdField()) )
				.groupBy( aoiIdField );
		Table<?> totals = selectTotals.asTable( "totals" );

		// select proportions
		SelectQuery<Record> select = psql().selectQuery();
		select.addSelect( phase1Table.getIdField().count() );
		select.addFrom( phase1Table );

		// join with phase1_aoi_table to calculate proportions. if no
		// stratification is applied, then results will be the same
		select.addSelect(aoiIdField);
		select.addJoin(phase1AoiTable,
				phase1Table.getIdField().eq(phase1AoiTable.getIdField()));
		select.addGroupBy(aoiIdField);

		if (stratified) {
			ColumnJoin stratumJoin = samplingDesign.getStratumJoin();

			Field<Integer> stratumField = phase1Table.getIntegerField(stratumJoin.getColumn());
			select.addSelect( stratumField.cast(SQLDataType.INTEGER).as( expf.STRATUM.getName() ) );
			select.addGroupBy( stratumField );
		}

		// join with totals inner query
		Field<?> totalField = totals.field("count");
		select.addSelect(totalField.as("total"));
		select.addJoin(totals, aoiIdField.eq((Field<Long>) totals.field(aoiIdField.getName())));
		select.addGroupBy(totalField);

		select.addSelect(phase1Table.getIdField().count()
				.div(DSL.cast(totalField, Psql.DOUBLE_PRECISION))
				.as(expf.PROPORTION.getName()));
		select.addSelect(phase1Table.getIdField().count()
				.div(DSL.cast(totalField, Psql.DOUBLE_PRECISION))
				.mul(aoiAreaField).as(expf.AREA.getName()));
		select.addGroupBy(aoiAreaField);
		// ( count(p.id) / total.count::double precision ) as proportion,
		// ( count(p.id) / total.count::double precision ) *
		// a._administrative_unit_level_1_area as area

		DropTableStep dropTableQuery 	= psql().dropTableIfExistsLegacy(expf);
		DbSendQuery dropTableScript 	= r().dbSendQuery(CONNECTION_VAR, dropTableQuery);
		r.addScript( dropTableScript );
		
		AsStep createTable 				= psql().createTableLegacy(expf).as(select);
		DbSendQuery createTableScript 	= r().dbSendQuery(CONNECTION_VAR, createTable);
		r.addScript( createTableScript );
		
		return r;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static RScript createExpfTable2stages( Workspace workspace , DataSchema schema, ExpansionFactorTable expf ) {
		RScript r								= r();
		
		SamplingDesign samplingDesign 			= workspace.getSamplingDesign();
		TwoStagesSettings sdSettings 			= samplingDesign.getTwoStagesSettingsObject();

		PrimarySamplingUnitTable<?> psuTable 	= samplingDesign.getPrimarySamplingUnitTable();
		ExtDataAoiTable psuAoiTable 			= schema.getPrimarySUAoiTable();

		AoiLevel aoiLevel 						= expf.getAoiLevel();
		boolean stratified 						= workspace.hasStratifiedSamplingDesign();

		Field<Long> aoiIdField 					= psuAoiTable.getAoiIdField(aoiLevel);

		Field<BigDecimal> aoiPsuTotal 			= psuAoiTable.getAoiAreaField(aoiLevel);

		// select totals for aoi
		SelectQuery<?> selectTotals = psql().selectQuery();
		selectTotals.addSelect(
				psuTable.getIdField().count()
						.as(expf.PSU_SAMPLED_TOTAL.getName()), aoiIdField,
				aoiPsuTotal.as(expf.PSU_TOTAL.getName()));
		selectTotals.addFrom(psuTable);
		Condition condition = null;
		for (Field<?> psuField : psuTable.getPsuFields()) {
			Field psuAoiField = psuAoiTable.field(psuField.getName());
			if (condition == null) {
				condition = psuField.eq(psuAoiField);
			} else {
				condition = condition.and(psuField.eq(psuAoiField));
			}
		}
		selectTotals.addJoin(psuAoiTable, condition);
		// .on( psuTable.getIdField().eq( psuAoiTable.getIdField() ) )
		selectTotals.addGroupBy(aoiIdField, aoiPsuTotal);
		Table<?> totals = selectTotals.asTable("totals");

		// main select
		SelectQuery<?> select = psql().selectQuery();
		select.addSelect(aoiIdField);
		// psu id field
		// select.addSelect( psuTable.getIdField() );
		// select.addSelect( psuTable.getPsuFields() );
		// select.addSelect(
		// psuTable.getVarcharField(sdSettings.getPsuIdColumns()) );
		select.addSelect(psuTable.getPsuFields());
		select.addFrom(psuTable);

		// join with aoi psu table
		Condition condition3 = null;
		for (Field<?> psuField : psuTable.getPsuFields()) {
			Field psuAoiField = psuAoiTable.field(psuField.getName());
			if (condition3 == null) {
				condition3 = psuField.eq(psuAoiField);
			} else {
				condition3 = condition3.and(psuField.eq(psuAoiField));
			}
		}
		select.addJoin(psuAoiTable, condition3);
		// select.addJoin( psuAoiTable, psuField.eq(psuAoiField) );
		// select.addJoin( psuAoiTable,
		// psuTable.getIdField().eq(psuAoiTable.getIdField()) );

		// TODO test with stratification
		if (stratified) {
			ColumnJoin stratumJoin 		= samplingDesign.getStratumJoin();

			Field<Integer> stratumField = psuTable.getIntegerField( stratumJoin.getColumn() );
			select.addSelect( stratumField.cast(SQLDataType.INTEGER).as( expf.STRATUM.getName() ) );
			select.addGroupBy( stratumField );
		}

		// add join with totals inner query
		select.addJoin(totals, aoiIdField.eq((Field<Long>) totals.field(aoiIdField.getName())));

		// select totals
		select.addSelect( totals.field(expf.PSU_TOTAL.getName()), totals.field(expf.PSU_SAMPLED_TOTAL.getName()) );

		// add bu_total
		// select psu area
		select.addSelect( psuTable.getBigDecimalField(sdSettings.getAreaColumn() ).as( expf.PSU_AREA.getName()) );

		// Secondary sampling unit counts
		Entity ssuEntity 				= workspace.getEntityByOriginalId( sdSettings.getSsuOriginalId() );
		EntityDataView ssuTable 		= schema.getDataView(ssuEntity);

		Entity baseUnit 				= workspace.getSamplingUnit();
		EntityDataView baseUnitTable 	= schema.getDataView( baseUnit );
		Field<Long> ssuIdField 			= baseUnitTable.getAncestorIdField( ssuEntity.getId() );
		
		Condition buPsuJoinConditions 	= psuTable.getJoinConditions( baseUnitTable, sdSettings.getJoinSettings() );

		Select<?> selectSSUCounts = psql()
				.select( psuTable.getIdField(),
						 ssuIdField.countDistinct().as( expf.SSU_COUNT.getName()) 
						)
				.from( psuTable )
				.join( baseUnitTable )
				.on( buPsuJoinConditions )
				.where( baseUnitTable.getWeightField().gt( BigDecimal.ZERO ) )
				.groupBy( psuTable.getIdField() );
		Table<?> countSSU = selectSSUCounts.asTable("countSSU");

		// add join with secondary sampling unit counts inner query
		select.addJoin(
				countSSU,
				psuTable.getIdField().eq(
						(Field<Long>) countSSU.field(psuTable.getIdField()
								.getName())));
		// select ssu count per ssu
		select.addSelect(countSSU.field(expf.SSU_COUNT.getName()));

		// SSU totals
		Condition psuSsuJoinConditions 	= psuTable.getJoinConditions( ssuTable, sdSettings.getJoinSettings() );

		SelectQuery<?> selectSSUTotals = psql().selectQuery();
		selectSSUTotals.addSelect(
				// psuTable.getIdField() ,
				aoiIdField,
				ssuTable.getIdField().count().as(expf.SSU_TOTAL.getName()));
		selectSSUTotals.addFrom(psuTable);
		selectSSUTotals.addJoin(ssuTable, psuSsuJoinConditions);

		Condition condition2 = null;
		for (Field<?> psuField : psuTable.getPsuFields()) {
			Field psuAoiField = psuAoiTable.field(psuField.getName());
			if (condition2 == null) {
				condition2 = psuField.eq(psuAoiField);
			} else {
				condition2 = condition2.and(psuField.eq(psuAoiField));
			}
		}
		selectSSUTotals.addJoin(psuAoiTable, condition);
		// .on( psuTable.getIdField().eq( psuAoiTable.getIdField() ) );
		selectSSUTotals.addGroupBy(aoiIdField);
		Table<?> totalSSU = selectSSUTotals.asTable("totalSSU");

		// add join with secondary sampling unit totals inner query
		select.addJoin(totalSSU, aoiIdField.eq((Field<Long>) totalSSU.field(aoiIdField.getName())));
		// select ssu count per ssu
		select.addSelect(totalSSU.field(expf.SSU_TOTAL.getName()));

		select.addSelect(psuTable.getNoTheoreticalBu().as( expf.NO_THEORETICAL_BU.getName()) );

		
		DropTableStep dropTableQuery 	= psql().dropTableIfExistsLegacy(expf);
		DbSendQuery dropTableScript 	= r().dbSendQuery(CONNECTION_VAR, dropTableQuery);
		r.addScript( dropTableScript );
		
		AsStep createTable 				= psql().createTableLegacy(expf).as(select);
		DbSendQuery createTableScript 	= r().dbSendQuery(CONNECTION_VAR, createTable);
		r.addScript( createTableScript );
		
		return r;

	}

	@SuppressWarnings("unchecked")
	private static RScript createExpfTable1phase( Workspace workspace , DataSchema schema, ExpansionFactorTable expf ) {
		RScript r 						= r();
		
		SamplingDesign samplingDesign 	= workspace.getSamplingDesign();
		DataAoiTable dataAoiTable 		= schema.getSamplingUnitAoiTable();
		EntityDataView dataView 		= schema.getDataView( samplingDesign.getSamplingUnit() );
		
		AoiLevel aoiLevel 				= expf.getAoiLevel();
		boolean stratified 				= workspace.hasStratifiedSamplingDesign();
		
		Field<Long> aoiIdField 			= dataAoiTable.getAoiIdField(aoiLevel);
		Field<BigDecimal> aoiAreaField 	= dataAoiTable.getAoiAreaField(aoiLevel);
		
		AoiTable a 						= Tables.AOI;
		AoiLevelTable l					= Tables.AOI_LEVEL;
		
		// select totals for aoi
		SelectQuery<Record> selectTotals = psql().selectQuery();
		
		selectTotals.addSelect( dataView.getWeightField().sum().as( "count" ) );
//		selectTotals.addSelect( dataView.getIdField().count() );
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
		
		
		Table<?> totals = selectTotals.asTable( "totals" );
//		
		// select proportions
		SelectQuery<Record> select = psql().selectQuery();
		
		select.addSelect( dataView.getIdField().count() );
		select.addFrom( dataView );
		
		// join with aoi_table to calculate proportions. if no stratification is applied, then results will be the same
		Field<Integer> totalsAoiIdField = (Field<Integer>) totals.field(aoiIdField.getName());
		select.addSelect( totalsAoiIdField  );
//		select.addSelect( dataAoiTable.getAoiAreaField(aoiLevel).as(expf.AREA.getName()) );
		select.addJoin( dataAoiTable, dataView.getIdField().eq(dataAoiTable.getIdField()) );
		select.addGroupBy( totalsAoiIdField );
		
		
		
		// join with totals inner query
		Field<?> totalField = totals.field("count");
		select.addSelect( totalField.as("total") );
		select.addJoin(totals , JoinType.RIGHT_OUTER_JOIN , aoiIdField.eq( (Field<Long>)totals.field(aoiIdField.getName())) );
		select.addGroupBy( totalField );
		
		Field<BigDecimal> countField = DSL.cast(totalField, Psql.DOUBLE_PRECISION);

		Field<BigDecimal> proportion = DSL.decode()
			.when( countField.gt(BigDecimal.ZERO), dataView.getWeightField().sum().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ) )
//			.when( countField.gt(BigDecimal.ZERO), dataView.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ) )
			.otherwise( BigDecimal.ZERO )
			.as( expf.PROPORTION.getName() );		
		select.addSelect( proportion );
		
		// add area field and stratum if there is
		if( stratified ) {
			ColumnJoin stratumJoin = samplingDesign.getStratumJoin();
			
			Field<Integer> stratumField = (Field<Integer>) dataView.field(stratumJoin.getColumn());
			Field<Integer> stratumFieldInteger = stratumField.cast(SQLDataType.INTEGER);
			select.addSelect( stratumFieldInteger.as(expf.STRATUM.getName()) );
			select.addGroupBy( stratumField );
			
			if( workspace.hasStrataAois() ){
				StratumTable S = StratumTable.STRATUM.as("s");
				select.addJoin( S , stratumFieldInteger.eq(S.STRATUM_NO).and(S.WORKSPACE_ID.eq(workspace.getId())) );
				
				StratumAoiTable SA = StratumAoiTable.STRATUM_AOI.as("sa");
				select.addJoin( SA , SA.AOI_ID.eq(totalsAoiIdField).and(SA.STRATUM_ID.eq(S.ID)) );
				
				select.addSelect( SA.AREA.as(expf.AREA.getName()) );
				select.addGroupBy( SA.AREA );
			} else {
				Field<BigDecimal> area = DSL.decode()
				.when( countField.gt(BigDecimal.ZERO), dataView.getWeightField().sum().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).mul(aoiAreaField) )
				.otherwise( BigDecimal.ZERO )
				.as( expf.AREA.getName() );
				
				select.addSelect( area );
				
				select.addGroupBy( aoiAreaField );
			}
			
		} else {
			select.addSelect( aoiAreaField.as(expf.AREA.getName()) );
			
			select.addGroupBy( aoiAreaField );
		}
		
//		Field<BigDecimal> area = DSL.decode()
//				.when( countField.gt(BigDecimal.ZERO), dataView.getWeightField().sum().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).mul(aoiAreaField) )
//				.otherwise( BigDecimal.ZERO )
//				.as( expf.AREA.getName() );
//		select.addSelect( area );
//		
//		select.addGroupBy( aoiAreaField );
//		( count(p.id) / total.count::double precision ) as proportion,
//        ( count(p.id) / total.count::double precision ) * a._administrative_unit_level_1_area as area
		
		DropTableStep dropTableQuery 	= psql().dropTableIfExistsLegacy(expf);
		DbSendQuery dropTableScript 	= r().dbSendQuery(CONNECTION_VAR, dropTableQuery);
		r.addScript( dropTableScript );
		
		AsStep createTable 				= psql().createTableLegacy(expf).as(select);
		DbSendQuery createTableScript 	= r().dbSendQuery(CONNECTION_VAR, createTable);
		r.addScript( createTableScript );
		
		return r;
			
		
	}
	
	@SuppressWarnings({ "unchecked" })
	private static RScript createClusterCountsTable( Workspace workspace , DataSchema schema ) {
		RScript r						= r();
		SamplingDesign samplingDesign 	= workspace.getSamplingDesign();
		Entity samplingUnit 			= workspace.getSamplingUnit();
//		Entity cluster 					= samplingDesign.getClusterEntity();
		EntityDataView samplingUnitView = schema.getDataView( samplingUnit );

//		AoiLevel aoiLevel 						= expf.getAoiLevel();
		ClusterCountsTable clusterCountsTable 	= schema.getClusterCountsTable(); 
		
		
		SelectQuery<?> select 	= psql().selectQuery();

		select.addFrom(samplingUnitView);
		
		Field<BigDecimal> plotWeightSum = samplingUnitView.getWeightField().sum();
		
		Field<BigDecimal> baseUnitWeight = DSL.decode().when(plotWeightSum.greaterThan(BigDecimal.ZERO), plotWeightSum).otherwise(BigDecimal.ZERO).as( clusterCountsTable.BASE_UNIT_WEIGHT.getName() );
		select.addSelect( baseUnitWeight );
		
		Field<BigDecimal> weight = DSL.decode().when(plotWeightSum.greaterThan(BigDecimal.ZERO), BigDecimal.ONE).otherwise(BigDecimal.ZERO).as( clusterCountsTable.WEIGHT.getName() );
		select.addSelect( weight );
		
//		Field<BigDecimal> cntField 	= samplingUnitView.getWeightField().sum().as("cnt");
//		Field<Integer> totField 	= samplingUnitView.getWeightField().count().as("total");
//		selectWeight.addSelect(cntField);
//		selectWeight.addSelect(totField);

		Field<Integer> stratumField = null;
//		if (samplingDesign.getTwoStages()) {

//			PrimarySamplingUnitTable<?> psuTable = samplingDesign.getPrimarySamplingUnitTable();
//			for (Field<?> field : psuTable.getPsuFields()) {
//				selectWeight.addSelect(field);
//				selectWeight.addGroupBy(field);
//			}
//
//			selectWeight.addJoin(psuTable, psuTable.getJoinConditions(
//					samplingUnitView, samplingDesign
//							.getTwoStagesSettingsObject().getJoinSettings()));

//		} else {
		DataAoiTable dataAoiTable = null;
		Field<Long> dataIdField;

		if (samplingDesign.getTwoPhases()) {
			DynamicTable<Record> phase1Table 	= new DynamicTable<Record>( workspace.getPhase1PlotTable(), "calc" );

			dataAoiTable 						= schema.getPhase1AoiTable();
			dataIdField 						= phase1Table.getIdField();

			// join with phase 1 table
			TableJoin phase1Join = samplingDesign.getPhase1Join();
			Condition conditions = null;
			for (int i = 0; i < phase1Join.getColumnJoinSize(); i++) {
				ColumnJoin leftColumn = phase1Join.getLeft()
						.getColumnJoins().get(i);
				ColumnJoin rightJoin = phase1Join.getRight()
						.getColumnJoins().get(i);
				Field<String> leftField = phase1Table
						.getVarcharField(leftColumn.getColumn());
				Field<String> rightField = (Field<String>) samplingUnitView
						.field(rightJoin.getColumn());

				Condition joinCondition = leftField.eq(rightField);
				if (conditions == null) {
					conditions = joinCondition;
				} else {
					conditions = conditions.and(joinCondition);
				}
			}
			select.addJoin(phase1Table, JoinType.RIGHT_OUTER_JOIN, conditions);
			select.setDistinct(true);

			if (samplingDesign.getStratified()) {
				stratumField = phase1Table.getIntegerField(samplingDesign.getStratumJoin().getColumn());
			}

		} else {
			dataAoiTable 	= schema.getSamplingUnitAoiTable();
			dataIdField 	= samplingUnitView.getIdField();

			if (samplingDesign.getStratified()) {
				stratumField = (Field<Integer>) samplingUnitView.field(samplingDesign.getStratumJoin().getColumn());
			}
		}

		// join with aoi
		Collection<Field<Long>> aoiIdFields = dataAoiTable.getAoiIdFields();
		select.addSelect( aoiIdFields );
		select.addJoin( dataAoiTable, dataAoiTable.getIdField().eq(dataIdField) );
		select.addGroupBy( aoiIdFields );

		if (stratumField != null) {
			select.addSelect( stratumField.cast(SQLDataType.INTEGER).as(clusterCountsTable.STRATUM.getName()) );
			select.addGroupBy( stratumField );
		}
		
		Field<Integer> clusterField = samplingUnitView.field(clusterCountsTable.CLUSTER_ID);
		select.addSelect( clusterField.as(clusterCountsTable.CLUSTER_ID.getName()) );
		select.addGroupBy( clusterField );
//		}

		AsStep createTable = psql()
			.createTableLegacy( clusterCountsTable )
			.as( select );
		
		DropTableStep dropTable = psql().dropTableIfExistsLegacy( clusterCountsTable );
		
		r.addScript( r().dbSendQuery(CONNECTION_VAR, dropTable) );
		r.addScript( r().dbSendQuery(CONNECTION_VAR, createTable) );

		return r;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static RScript addWeightField( Workspace workspace , DataSchema schema, ExpansionFactorTable expf ) {
		RScript r						= r();
		SamplingDesign samplingDesign 	= workspace.getSamplingDesign();

		Entity samplingUnit 			= workspace.getSamplingUnit();
		EntityDataView samplingUnitView = schema.getDataView( samplingUnit );

		AoiLevel aoiLevel 				= expf.getAoiLevel();

		SelectQuery<?> selectWeight 	= psql().selectQuery();

		selectWeight.addFrom(samplingUnitView);
		
		Field<BigDecimal> cntField 	= samplingUnitView.getWeightField().sum().as("cnt");
		Field<Integer> totField 	= samplingUnitView.getWeightField().count().as("total");
		selectWeight.addSelect(cntField);
		selectWeight.addSelect(totField);

		Field<Integer> stratumField = null;
		if (samplingDesign.getTwoStages()) {

			PrimarySamplingUnitTable<?> psuTable = samplingDesign.getPrimarySamplingUnitTable();
			for (Field<?> field : psuTable.getPsuFields()) {
				selectWeight.addSelect(field);
				selectWeight.addGroupBy(field);
			}

			selectWeight.addJoin(psuTable, psuTable.getJoinConditions(
					samplingUnitView, samplingDesign
							.getTwoStagesSettingsObject().getJoinSettings()));

		} else {
			DataAoiTable dataAoiTable = null;
			Field<Long> dataIdField;

			if (samplingDesign.getTwoPhases()) {
				DynamicTable<Record> phase1Table 	= new DynamicTable<Record>( workspace.getPhase1PlotTable(), "calc" );

				dataAoiTable 						= schema.getPhase1AoiTable();
				dataIdField 						= phase1Table.getIdField();

				// join with phase 1 table
				TableJoin phase1Join = samplingDesign.getPhase1Join();
				Condition conditions = null;
				for (int i = 0; i < phase1Join.getColumnJoinSize(); i++) {
					ColumnJoin leftColumn = phase1Join.getLeft()
							.getColumnJoins().get(i);
					ColumnJoin rightJoin = phase1Join.getRight()
							.getColumnJoins().get(i);
					Field<String> leftField = phase1Table
							.getVarcharField(leftColumn.getColumn());
					Field<String> rightField = (Field<String>) samplingUnitView
							.field(rightJoin.getColumn());

					Condition joinCondition = leftField.eq(rightField);
					if (conditions == null) {
						conditions = joinCondition;
					} else {
						conditions = conditions.and(joinCondition);
					}
				}
				selectWeight.addJoin(phase1Table, JoinType.RIGHT_OUTER_JOIN,
						conditions);
				selectWeight.setDistinct(true);

				if (samplingDesign.getStratified()) {
					stratumField = phase1Table.getIntegerField(samplingDesign
							.getStratumJoin().getColumn());
				}

			} else {
				dataAoiTable 	= schema.getSamplingUnitAoiTable();
				dataIdField 	= samplingUnitView.getIdField();

				if (samplingDesign.getStratified()) {
					stratumField = (Field<Integer>) samplingUnitView
							.field(samplingDesign.getStratumJoin().getColumn());
				}
			}

			// join with aoi
			Field<Long> aoiIdField = dataAoiTable.getAoiIdField(aoiLevel);
			selectWeight.addSelect(aoiIdField);
			selectWeight.addJoin( dataAoiTable, dataAoiTable.getIdField().eq(dataIdField) );
			selectWeight.addGroupBy( aoiIdField );

			if (stratumField != null) {
				selectWeight.addSelect( stratumField.cast(SQLDataType.INTEGER).as(stratumField.getName()) );
				selectWeight.addGroupBy( stratumField );
			}

		}

		// add weight column and base unit total
		AddColumnStep addColumnW = psql().alterTableLegacy(expf).addColumn( expf.WEIGHT);
		r.addScript( r().dbSendQuery(CONNECTION_VAR, addColumnW) );
		
		AddColumnStep addColumnBU = psql().alterTableLegacy(expf).addColumn( expf.BU_TOTAL);
		r.addScript( r().dbSendQuery(CONNECTION_VAR, addColumnBU) );

		// update weight
		Table<?> cursor = selectWeight.asTable("tmp");
		// cursor.field( expf.AOI_ID. );
		Update<?> update = psql()
				.update(expf)
				.set( expf.WEIGHT, (Field<BigDecimal>) cursor.field(cntField.getName()) )
				.set( expf.BU_TOTAL, (Field<BigDecimal>) cursor.field(totField.getName()) );

		Condition joinCondition = null;
		if (samplingDesign.getTwoStages()) {
			PrimarySamplingUnitTable<?> psuTable = samplingDesign.getPrimarySamplingUnitTable();

			for (Field<?> field : psuTable.getPsuFields()) {
				String fieldName = field.getName();
				Field expfField = expf.field(fieldName);
				Condition psuJoinCondition = expfField.eq( cursor.field(fieldName) );

				if ( joinCondition == null ){
					joinCondition = psuJoinCondition;
				} else {
					joinCondition = joinCondition.and(psuJoinCondition);
				}
			}
		} else {
			joinCondition = expf.AOI_ID.eq( (Field<Integer>) cursor.field(expf.AOI_ID.getName()) );
		}
		if (samplingDesign.getStratified()) {
			joinCondition = joinCondition.and( expf.STRATUM.eq((Field<Integer>) cursor.field(stratumField.getName())) );
		}

		// execute update
		UpdateWithStep updateWith = psql().updateWith( cursor, update, joinCondition );
		r.addScript( r().dbSendQuery(CONNECTION_VAR, updateWith) );
		
		// if ws two stages, add bu_total as well
		if (samplingDesign.getTwoStages()) {
			
			UpdateSetMoreStep<Record> setBUTotal = psql()
				.update( expf )
				.set( expf.BU_TOTAL , expf.SSU_COUNT.mul( expf.NO_THEORETICAL_BU ) );
			r.addScript( r().dbSendQuery( CONNECTION_VAR, setBUTotal) );
			
		}
		
		// if cluster sampling, add cluster weights 
		if( workspace.hasClusterSamplingDesign() ){
			// add column
			AddColumnStep addColumn = psql().alterTableLegacy( expf ).addColumn(expf.CLUSTER_WEIGHT);
			r.addScript( r().dbSendQuery( CONNECTION_VAR, addColumn) );
			
			ClusterCountsTable clusterCountsTable = schema.getClusterCountsTable();
			
			// select sum of cluster weights from cluster_counts_table
			SelectQuery<Record> select = psql().selectQuery();
			select.addFrom( clusterCountsTable );
			
			select.addSelect( clusterCountsTable.WEIGHT.sum().as("cluster_weight") );
			
			Field<Integer> aoiIdField = clusterCountsTable.getAoiIdField(aoiLevel);
			select.addSelect( aoiIdField );
			select.addGroupBy( aoiIdField );
			
			if( workspace.hasStratifiedSamplingDesign() ){
				select.addSelect( clusterCountsTable.STRATUM );
				select.addGroupBy( clusterCountsTable.STRATUM );
			}
			Table<Record> clusterCounts = select.asTable("cluster_counts");
			
			Update<?> updateClusterWeight = psql()
					.update(expf)
					.set(expf.CLUSTER_WEIGHT, (Field<BigDecimal>)clusterCounts.field("cluster_weight"));
			
			
			Condition updateClusterWeightJoin = expf.AOI_ID.eq((Field<Integer>) clusterCounts.field(aoiIdField) );
			if( workspace.hasStratifiedSamplingDesign() ){
				updateClusterWeightJoin = updateClusterWeightJoin.and( expf.STRATUM.eq((Field<Integer>) clusterCounts.field(clusterCountsTable.STRATUM.getName())) );
			}
			
			// update expf cluster_weight_column
			UpdateWithStep upd = psql().updateWith( clusterCounts, updateClusterWeight , updateClusterWeightJoin );
			r.addScript( r().dbSendQuery( CONNECTION_VAR, upd) );
		}
		
		return r;
	}

	private static RScript addExpfField( Workspace workspace , ExpansionFactorTable expf) {
		RScript r 					= new RScript();
		AddColumnStep addColumn 	= psql().alterTableLegacy( expf ).addColumn( expf.EXPF );
		
		r.addScript( r().dbSendQuery(CONNECTION_VAR, addColumn) );
		
		Field<BigDecimal> expfFormula = null;

		if ( workspace.has2StagesSamplingDesign() ) {

			expfFormula = expf.PSU_TOTAL.div(expf.PSU_SAMPLED_TOTAL)
					.mul(expf.PSU_AREA.div(expf.SSU_COUNT))
					.mul(expf.BU_TOTAL.div(expf.WEIGHT));

		} else {
			boolean clusterSampling = workspace.hasClusterSamplingDesign() && !workspace.getSamplingDesign().applyClusterOnlyForErrorCalculation();
			TableField<Record, BigDecimal> weightField = (clusterSampling) ? expf.CLUSTER_WEIGHT : expf.WEIGHT;
			
			expfFormula = DSL
					.decode()
					.when(weightField.gt(BigDecimal.ZERO),
							expf.AREA.div(weightField))
					.otherwise(BigDecimal.ZERO);

		}

		UpdateSetMoreStep<Record> setExpf = psql().update(expf).set(expf.EXPF, expfFormula );
		r.addScript( r().dbSendQuery(CONNECTION_VAR, setExpf) );
		
		return r;
	}


}

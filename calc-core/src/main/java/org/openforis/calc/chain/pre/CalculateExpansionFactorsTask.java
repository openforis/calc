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
import org.jooq.impl.PrimarySamplingUnitTable;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Task;
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
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.ExtDataAoiTable;

/**
 * Task responsible for calculating the expansion factor for each stratum in all
 * AOI levels. Results will be stored in a table called _expf in the output
 * schema
 * 
 * @author M. Togna
 */
@Deprecated
public final class CalculateExpansionFactorsTask extends Task {

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
		for (AoiLevel aoiLevel : hierarchy.getLevels()) {
			ExpansionFactorTable expf = schema
					.getExpansionFactorTable(aoiLevel);

			// create expf table
			if (samplingDesign.getTwoPhases()) {
				createExpfTable2phases(expf);
			} else if (samplingDesign.getTwoStages()) {
				createExpfTable2stages(expf);
			} else {
				createExpfTable1phase(expf);
			}

			addWeightField(expf);
			addExpfField(expf);

			incrementItemsProcessed();
		}
	}

	private void addExpfField(ExpansionFactorTable expf) {
		psql().alterTable(expf).addColumn(expf.EXPF).execute();
		Field<BigDecimal> expfFormula = null;

		if (getWorkspace().has2StagesSamplingDesign()) {

			expfFormula = expf.PSU_TOTAL.div(expf.PSU_SAMPLED_TOTAL)
					.mul(expf.PSU_AREA.div(expf.SSU_COUNT))
					.mul(expf.BU_TOTAL.div(expf.WEIGHT));
			// .div( expf.BU_TOTAL );

			// .mul(
			// // expf.SSU_TOTAL.div( expf.BU_TOTAL )
			// )

		} else {

			expfFormula = DSL
					.decode()
					.when(expf.WEIGHT.gt(BigDecimal.ZERO),
							expf.AREA.div(expf.WEIGHT))
					.otherwise(BigDecimal.ZERO);

		}

		psql().update(expf).set(expf.EXPF, expfFormula

		).execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addWeightField(ExpansionFactorTable expf) {
		Workspace workspace = getWorkspace();
		SamplingDesign samplingDesign = workspace.getSamplingDesign();

		Entity samplingUnit = workspace.getSamplingUnit();
		EntityDataView samplingUnitView = getInputSchema().getDataView(
				samplingUnit);

		AoiLevel aoiLevel = expf.getAoiLevel();

		SelectQuery<?> selectWeight = psql().selectQuery();

		selectWeight.addFrom(samplingUnitView);
		Field<BigDecimal> cntField = samplingUnitView.getWeightField().sum()
				.as("cnt");
		Field<Integer> totField = samplingUnitView.getWeightField().count()
				.as("total");
		selectWeight.addSelect(cntField);
		selectWeight.addSelect(totField);

		Field<Integer> stratumField = null;
		if (samplingDesign.getTwoStages()) {

			PrimarySamplingUnitTable<?> psuTable = samplingDesign
					.getPrimarySamplingUnitTable();
			// String psuIdColumn = sdSettings.getSamplingUnitPsuJoinColumns();
			// Field<?> psuIdField = samplingUnitView.field( psuIdColumn );
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
				DynamicTable<Record> phase1Table = new DynamicTable<Record>(
						workspace.getPhase1PlotTable(), "calc");

				dataAoiTable = getInputSchema().getPhase1AoiTable();
				dataIdField = phase1Table.getIdField();

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
				dataAoiTable = getInputSchema().getSamplingUnitAoiTable();
				dataIdField = samplingUnitView.getIdField();

				if (samplingDesign.getStratified()) {
					stratumField = (Field<Integer>) samplingUnitView
							.field(samplingDesign.getStratumJoin().getColumn());
				}
			}

			// join with aoi
			Field<Long> aoiIdField = dataAoiTable.getAoiIdField(aoiLevel);
			selectWeight.addSelect(aoiIdField);
			selectWeight.addJoin(dataAoiTable,
					dataAoiTable.getIdField().eq(dataIdField));
			selectWeight.addGroupBy(aoiIdField);

			if (stratumField != null) {
				selectWeight.addSelect(stratumField.cast(SQLDataType.INTEGER)
						.as(stratumField.getName()));
				selectWeight.addGroupBy(stratumField);
			}

		}

		// add weight column and base unit total
		psql().alterTable(expf).addColumn(expf.WEIGHT).execute();

		psql().alterTable(expf).addColumn(expf.BU_TOTAL).execute();

		// update weight
		Table<?> cursor = selectWeight.asTable("tmp");
		// cursor.field( expf.AOI_ID. );
		Update<?> update = psql()
				.update(expf)
				.set(expf.WEIGHT,
						(Field<BigDecimal>) cursor.field(cntField.getName()))
				.set(expf.BU_TOTAL,
						(Field<BigDecimal>) cursor.field(totField.getName()));

		Condition joinCondition = null;
		if (samplingDesign.getTwoStages()) {
			PrimarySamplingUnitTable<?> psuTable = samplingDesign
					.getPrimarySamplingUnitTable();

			for (Field<?> field : psuTable.getPsuFields()) {
				String fieldName = field.getName();
				Field expfField = expf.field(fieldName);
				Condition psuJoinCondition = expfField.eq(cursor
						.field(fieldName));

				if (joinCondition == null) {
					joinCondition = psuJoinCondition;
				} else {
					joinCondition = joinCondition.and(psuJoinCondition);
				}
			}
		} else {
			joinCondition = expf.AOI_ID.eq((Field<Integer>) cursor
					.field(expf.AOI_ID.getName()));
		}
		if (samplingDesign.getStratified()) {
			joinCondition = joinCondition.and(expf.STRATUM
					.eq((Field<Integer>) cursor.field(stratumField.getName())));
		}

		// execute update
		UpdateWithStep updateWith = psql().updateWith(cursor, update,
				joinCondition);
		updateWith.execute();

		// if ws two stages, add bu_total as well
		if (samplingDesign.getTwoStages()) {
			// psql().alterTable( expf ).addColumn( expf.BU_TOTAL ).execute();

			
			psql()
				.update( expf )
				.set( expf.BU_TOTAL , expf.SSU_COUNT.mul( expf.NO_THEORETICAL_BU ) )
				.execute();
			
//			SelectQuery<Record> selectBUTotals = psql().selectQuery();
//			String weight = expf.WEIGHT.getName();
//			selectBUTotals.addSelect(expf.WEIGHT.sum().as(weight));
//			selectBUTotals.addSelect(expf.AOI_ID);
//			selectBUTotals.addFrom(expf);
//			selectBUTotals.addGroupBy(expf.AOI_ID);
//			if (getWorkspace().hasStratifiedSamplingDesign()) {
//				selectBUTotals.addSelect(expf.STRATUM);
//				selectBUTotals.addGroupBy(expf.STRATUM);
//			}
//
//			Table<Record> buTotals = selectBUTotals.asTable("totals");
//
//			//
//			// Update<?> updateBUTotal = psql()
//			// .update( expf )
//			// .set( expf.BU_TOTAL , (Field<BigDecimal>) buTotals.field(weight)
//			// );
//			//
//
//			Condition join = expf.AOI_ID.eq((Field<Integer>) buTotals
//					.field(expf.AOI_ID.getName()));
//			if (samplingDesign.getStratified()) {
//				join = join.and(expf.STRATUM.eq((Field<Integer>) buTotals
//						.field(expf.STRATUM.getName())));
//			}

			// UpdateWithStep updateWith2 = psql()
			// .updateWith( buTotals, updateBUTotal , join );
			// updateWith2
			// .execute();
		}

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
//			select.addSelect( stratumField.as(expf.STRATUM.getName()) );
			select.addSelect( stratumField.cast(SQLDataType.INTEGER).as(expf.STRATUM.getName()) );
			select.addGroupBy( stratumField );
		}
		
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
		
		Field<BigDecimal> area = DSL.decode()
				.when( countField.gt(BigDecimal.ZERO), dataView.getWeightField().sum().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).mul(aoiAreaField) )
//				.when( countField.gt(BigDecimal.ZERO), dataView.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).mul(aoiAreaField) )
				.otherwise( BigDecimal.ZERO )
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
	private void createExpfTable2phases(ExpansionFactorTable expf) {
		DynamicTable<Record> phase1Table = new DynamicTable<Record>(
				getWorkspace().getPhase1PlotTable(), "calc");
		ExtDataAoiTable phase1AoiTable = getInputSchema().getPhase1AoiTable();
		AoiLevel aoiLevel = expf.getAoiLevel();
		boolean stratified = getWorkspace().hasStratifiedSamplingDesign();

		Field<Long> aoiIdField = phase1AoiTable.getAoiIdField(aoiLevel);
		Field<BigDecimal> aoiAreaField = phase1AoiTable
				.getAoiAreaField(aoiLevel);

		// select totals for aoi
		Select<?> selectTotals = psql()
				.select(phase1Table.getIdField().count(), aoiIdField)
				.from(phase1Table).join(phase1AoiTable)
				.on(phase1Table.getIdField().eq(phase1AoiTable.getIdField()))
				.groupBy(aoiIdField);
		Table<?> totals = selectTotals.asTable("totals");

		// select proportions
		SelectQuery<Record> select = psql().selectQuery();
		select.addSelect(phase1Table.getIdField().count());
		select.addFrom(phase1Table);

		// join with phase1_aoi_table to calculate proportions. if no
		// stratification is applied, then results will be the same
		select.addSelect(aoiIdField);
		select.addJoin(phase1AoiTable,
				phase1Table.getIdField().eq(phase1AoiTable.getIdField()));
		select.addGroupBy(aoiIdField);

		if (stratified) {
			ColumnJoin stratumJoin = getSamplingDesign().getStratumJoin();

			Field<Integer> stratumField = phase1Table
					.getIntegerField(stratumJoin.getColumn());
			select.addSelect(stratumField.cast(SQLDataType.INTEGER).as(
					expf.STRATUM.getName()));
			select.addGroupBy(stratumField);
		}

		// join with totals inner query
		Field<?> totalField = totals.field("count");
		select.addSelect(totalField.as("total"));
		select.addJoin(totals,
				aoiIdField.eq((Field<Long>) totals.field(aoiIdField.getName())));
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

		psql().dropTableIfExists(expf).execute();

		psql().createTable(expf).as(select).execute();

	}

	@SuppressWarnings("unchecked")
	private void createExpfTable2stages(ExpansionFactorTable expf) {
		DataSchema dataSchema = getInputSchema();

		Workspace workspace = getWorkspace();
		TwoStagesSettings sdSettings = workspace.getSamplingDesign().getTwoStagesSettingsObject();

		PrimarySamplingUnitTable<?> psuTable = workspace.getSamplingDesign().getPrimarySamplingUnitTable();
		ExtDataAoiTable psuAoiTable = dataSchema.getPrimarySUAoiTable();

		AoiLevel aoiLevel = expf.getAoiLevel();
		boolean stratified = workspace.hasStratifiedSamplingDesign();

		Field<Long> aoiIdField = psuAoiTable.getAoiIdField(aoiLevel);

		Field<BigDecimal> aoiPsuTotal = psuAoiTable.getAoiAreaField(aoiLevel);

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
			ColumnJoin stratumJoin = getSamplingDesign().getStratumJoin();

			Field<Integer> stratumField = psuTable.getIntegerField(stratumJoin
					.getColumn());
			select.addSelect(stratumField.cast(SQLDataType.INTEGER).as(
					expf.STRATUM.getName()));
			select.addGroupBy(stratumField);
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
		EntityDataView ssuTable 		= dataSchema.getDataView(ssuEntity);

		Entity baseUnit 				= workspace.getSamplingUnit();
		EntityDataView baseUnitTable 	= dataSchema.getDataView( baseUnit );
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
		select.addJoin(totalSSU, aoiIdField.eq((Field<Long>) totalSSU
				.field(aoiIdField.getName())));
		// select ssu count per ssu
		select.addSelect(totalSSU.field(expf.SSU_TOTAL.getName()));

		// selectOLD.addGroupBy( aoiIdField );

		select.addSelect(psuTable.getNoTheoreticalBu().as(
				expf.NO_THEORETICAL_BU.getName()));

		// join with totals
		// Field<?> totalField = totals.field("count");
		// selectOLD.addGroupBy( totalField );

		psql().dropTableIfExists(expf).execute();

		psql().createTable(expf).as(select).execute();

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

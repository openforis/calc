package org.openforis.calc.chain.post;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign.TableJoin;
import org.openforis.calc.psql.CreateTableStep.AsStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.AoiAggregateTable;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.NewFactTable;
import org.openforis.calc.schema.SamplingUnitAggregateTable;
import org.openforis.calc.schema.Schemas;

/**
 * Creates and populates aggregate tables for sampling unit entities and descendants. One for each AOI level (at AOI/stratum level) is created.
 * 
 * @author M. Togna
 */
public final class CreateAggregateTablesTask extends Task {
	
//	private Entity entity;

	public CreateAggregateTablesTask() {
		super();
//		this.entity = entity;
	}

	@Override
	protected long countTotalItems() {
		Schemas schemas = getJob().getSchemas();
		List<NewFactTable> factTables = schemas.getInputSchema().getFactTables();
		return factTables.size();
	}
	
	protected void execute() throws Throwable {
		InputSchema schema = getDataSchema();
		
		List<NewFactTable> factTables = schema.getFactTables();
		for (NewFactTable factTable : factTables) {
			// create fact table
			createFactTable(factTable);
		
			// create plot aggregate table
			SamplingUnitAggregateTable suAggregateTable = factTable.getSamplingUnitAggregateTable();
			if( suAggregateTable != null ){
				createSamplingUnitAggregateTable(suAggregateTable);
			}
			// create aggregation tables for each aoi level if there are
			createAoiAggregateTables( factTable );
			
			incrementItemsProcessed();
		}
		
	}

	protected InputSchema getDataSchema() {
		Job job = getJob();
		Schemas schemas = job.getSchemas();
		InputSchema schema = schemas.getInputSchema();
		return schema;
	}

	private void createAoiAggregateTables(NewFactTable factTable) {
		Collection<AoiAggregateTable> aggregateTables = factTable.getAoiAggregateTables();
		for ( AoiAggregateTable aggTable : aggregateTables ) {
			
			DataTable sourceTable = aggTable.getSourceTable();
			SelectQuery<Record> select = psql().selectQuery();
			
			select.addFrom( sourceTable );
			
//			select.addSelect( sourceTable.getDimensionIdFields() );
			for (Field<Integer> dimField : sourceTable.getDimensionIdFields()) {
				select.addSelect( DSL.coalesce(dimField,-1).as( dimField.getName() ) );
			}
			select.addGroupBy( sourceTable.getDimensionIdFields() );
			
			Collection<Field<Integer>> aoiIdFields = aggTable.getAoiIdFields();
			for (Field<Integer> aoiField : aoiIdFields) {
				Field<?> field = sourceTable.field(aoiField.getName());
				select.addSelect( field );
				select.addGroupBy( field );				
			}

			// join with expf table
			AoiLevel aoiLevel = aggTable.getAoiLevel();
			ExpansionFactorTable expfTable = getDataSchema().getExpansionFactorTable( aoiLevel );

			Field<Integer> aoiField = sourceTable.getAoiIdField(aoiLevel);
			Condition conditions = expfTable.AOI_ID.eq( aoiField );
			if( getWorkspace().getSamplingDesign().getStratified() ){
				
				conditions = conditions.and( expfTable.STRATUM.eq( sourceTable.getStratumField() ) );

				select.addSelect( sourceTable.getStratumField() );
				select.addGroupBy( sourceTable.getStratumField() );
			}
			select.addJoin( expfTable, conditions );
			select.addGroupBy( expfTable.EXPF );
			
			// add sum( quantity * expf )
			// for now quantity fields. check if it needs to be done for each variable aggregate
			for ( QuantitativeVariable var : sourceTable.getEntity().getOutputVariables() ) {
				Field<BigDecimal> quantityField = sourceTable.getQuantityField(var);				
				Field<BigDecimal> aggregateField = quantityField.mul( expfTable.EXPF ).sum();
				
				select.addSelect( aggregateField.as(quantityField.getName() ) );
			}
			
			// aggregate count field (used by mondrian)
			select.addSelect( DSL.count().as(aggTable.getAggregateFactCountField().getName()) );
			
			psql()
				.dropTableIfExists(aggTable)
				.execute();
			
			psql()
				.createTable(aggTable)
				.as(select)
				.execute();
		}
		
	}

	private void createSamplingUnitAggregateTable(SamplingUnitAggregateTable suAggTable ) {
//		SamplingUnitAggregateTable plotAgg = factTable.getPlotAggregateTable();
		DataTable sourceTable = suAggTable.getSourceTable();
		SelectQuery<Record> select = psql().selectQuery();
		
		select.addFrom( sourceTable );
		
		select.addSelect( sourceTable.getParentIdField() );
		select.addGroupBy( sourceTable.getParentIdField() );
//		select.addSelect( sourceTable.getDimensionIdFields() );
		for (Field<Integer> dimField : sourceTable.getDimensionIdFields()) {
			select.addSelect( DSL.coalesce(dimField,-1).as( dimField.getName() ) );
		}
		select.addGroupBy( sourceTable.getDimensionIdFields() );
		select.addSelect( sourceTable.getAoiIdFields() );
		select.addGroupBy( sourceTable.getAoiIdFields() );
		select.addSelect( sourceTable.getStratumField() );
		select.addGroupBy( sourceTable.getStratumField() );
		
		// for now quantity fields. check if it needs to be done for each variable aggregate
		Field<BigDecimal> plotArea = ((NewFactTable)sourceTable) .getPlotAreaField();
		for ( QuantitativeVariable var : sourceTable.getEntity().getOutputVariables() ) {
			Field<BigDecimal> quantityField = sourceTable.getQuantityField(var);
			
			Field<BigDecimal> aggregateField = 
				DSL.sum(
					DSL.decode()
					.when( plotArea.notEqual(BigDecimal.ZERO), quantityField.div(plotArea) )
					.otherwise( BigDecimal.ZERO )
				).as( quantityField.getName() );

			select.addSelect( aggregateField );
		}
		
		// drop table
		psql()
			.dropTableIfExists( suAggTable )
			.execute();
		
		AsStep createTable = psql()
			.createTable(suAggTable)
			.as(select);
		
		createTable.execute();
	}	
	
	
	
	private void createFactTable(NewFactTable factTable) {
		EntityDataView dataTable = factTable.getEntityView();
//		Entity entity = dataTable.getEntity();
		
		SelectQuery<?> select = new Psql().selectQuery(dataTable);
		select.addSelect(dataTable.getIdField());
		select.addSelect(dataTable.getParentIdField());
		// select.addSelect(dataTable.getAoiIdFields());
		for (Field<Integer> field : factTable.getDimensionIdFields()) {
			// todo add dim fields to entitydataview
			select.addSelect(dataTable.field(field));
		}

		// select measure
		List<QuantitativeVariable> vars = factTable.getEntity().getQuantitativeVariables();
		for (QuantitativeVariable var : vars) {
			Field<BigDecimal> fld = dataTable.getQuantityField(var);
			select.addSelect(fld);

			// for (VariableAggregate agg : var.getAggregates()) {
			// Field<BigDecimal> measureFld = factTable.getVariableAggregateField(agg);
			// Field<BigDecimal> valueFld = dataTable.getQuantityField(var);
			// select.addSelect( valueFld.as(measureFld.getName()) );
			// // }
			// }
		}

		// add plot area
		Field<BigDecimal> plotAreaField = factTable.getPlotAreaField();
		if (plotAreaField != null) {
			select.addSelect( dataTable.field(plotAreaField) );
		}
		
		// add aoi ids to fact table if it's geo referenced
		if( factTable.isGeoreferenced() ) {
			DataAoiTable aoiTable = getJob().getInputSchema().getSamplingUnitAoiTable();
			select.addSelect( aoiTable.getAoiIdFields() );
			
			Field<Long> joinField = ( dataTable.getEntity().isSamplingUnit() ) ? dataTable.getIdField() : dataTable.getParentIdField();
			select.addJoin(aoiTable, joinField.eq(aoiTable.getIdField()) );
		}
		
		// add stratum column if sampling design is stratified
		if( getWorkspace().getSamplingDesign().getStratified() ){
			Field<Integer> stratumField = null;
			String stratumColumn = getWorkspace().getSamplingDesign().getStratumJoin().getColumn();
			
			if( getWorkspace().getSamplingDesign().getTwoPhases() ){
				
				DynamicTable<Record> phase1Table = factTable.getDataSchema().getPhase1Table();
				TableJoin phase1Join = getWorkspace().getSamplingDesign().getPhase1Join();
				Condition conditions = phase1Table.getJoinConditions( dataTable, phase1Join );
				select.addJoin(phase1Table, conditions);
				
				stratumField = phase1Table.getIntegerField( stratumColumn ).cast(Integer.class).as( factTable.getStratumField().getName() ) ;
			} else {
				stratumField = dataTable.field( stratumColumn ).cast(Integer.class).as( factTable.getStratumField().getName() ) ;
			}
			
			select.addSelect( stratumField );
		}
		
		
		// drop table
		psql().dropTableIfExists( factTable ).execute();

		// create table
		AsStep createTable = psql().createTable( factTable ).as( select );
		createTable.execute();

		// Grant access to system user
		psql().grant(Privilege.ALL).on(factTable).to(getSystemUser()).execute();
	}
	
	
	
	@Override
	public String getName() {
		return String.format( "Create aggregate tables" );
	}
	
//	@Override
//	protected void oldExecute() throws Throwable {
//		// TODO threshold
//		OutputSchema outputSchema = getOutputSchema();
//		Collection<AggregateTable> aggTables = outputSchema.getAggregateTables();
//		ExpansionFactorTable expf = outputSchema.getExpansionFactorTable();
//		for (AggregateTable aggTable : aggTables) {
//			AoiLevel level = null;// aggTable.getAoiHierarchyLevel();
//			NewFactTable f = (NewFactTable) aggTable.getSourceTable();
//			Field<Integer> aoiId = f.getAoiIdField(level);
//			Field<Integer> stratumId = f.getStratumField();
//			Entity entity = aggTable.getEntity();
//			Integer entityId = entity.getId();
//			
//			SelectQuery<?> select = new Psql().selectQuery(f);
//			select.addSelect(f.getCategoryValueFields());
//			select.addSelect(f.getDimensionIdFields());
//			select.addSelect(stratumId);
//			
//			// Select AOI ID columns
//			Collection<Field<Integer>> aoiIdFields = aggTable.getAoiIdFields();
//			for (Field<Integer> aoiIdField : aoiIdFields) {
//				select.addSelect(f.field(aoiIdField));
//			}
//			
//			select.addGroupBy( select.getSelect() );
//			select.addGroupBy( expf.EXPF );
//			
//			// Add aggregate columns
//			List<VariableAggregate> variableAggregates = entity.getVariableAggregates();
//			for (VariableAggregate varAgg : variableAggregates) {
//				if( !varAgg.isVirtual() ){
//					String formula = varAgg.getAggregateFormula();
//					String aggCol = varAgg.getAggregateColumn();
//					select.addSelect(DSL.field(formula).as(aggCol));
//				}
//			}
//			
//			//add aggregate fact count column
//			select.addSelect(DSL.count().as(aggTable.getAggregateFactCountField().getName()));
//			
//			if ( isDebugMode() ) {
//				psql()
//					.dropTableIfExists(aggTable)
//					.execute();
//				
//
//			select.addJoin(expf, stratumId.eq(expf.STRATUM_ID)
//				  .and(aoiId.eq(expf.AOI_ID))
//				  .and(expf.ENTITY_ID.eq(entityId)));
//
//			psql().createTable(aggTable).as(select).execute();
//
//			// Grant access to system user
//			psql()
//				.grant(Privilege.ALL)
//				.on(aggTable)
//				.to(getSystemUser())
//				.execute();
//				
//			}
//		}
//	}
}

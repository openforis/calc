package org.openforis.calc.chain.post;

import static org.jooq.util.postgres.PostgresDataType.DOUBLEPRECISION;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.CalculationException;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.metadata.SamplingDesign.TableJoin;
import org.openforis.calc.psql.CreateTableStep.AsStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.AoiAggregateTable;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ErrorTable;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.FactTable;
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
		List<FactTable> factTables = schemas.getDataSchema().getFactTables();
		return factTables.size();
	}
	
	protected void execute() throws Throwable {
		DataSchema schema = getDataSchema();
		
		List<FactTable> factTables = schema.getFactTables();
		for ( FactTable factTable : factTables ){
			// create fact table
			createFactTable( factTable );
		
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

	protected DataSchema getDataSchema() {
		Job job = getJob();
		Schemas schemas = job.getSchemas();
		DataSchema schema = schemas.getDataSchema();
		return schema;
	}

	@SuppressWarnings("unchecked")
	private void createAoiAggregateTables(FactTable factTable) {
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
			AoiLevel aoiLevel 				= aggTable.getAoiLevel();
			ExpansionFactorTable expfTable 	= getDataSchema().getExpansionFactorTable( aoiLevel );

			Condition conditions = null;
			if( getWorkspace().has2StagesSamplingDesign() ){
				
//				TwoStagesSettings sdSettings 			= getWorkspace().getSamplingDesign().getTwoStagesSettingsObject();
				SamplingDesign samplingDesign = getWorkspace().getSamplingDesign();
//				PrimarySamplingUnitTable<?> psuTable 	= samplingDesign.getPrimarySamplingUnitTable();
				List<ColumnJoin> psuJoinColumns = samplingDesign.getTwoStagesSettingsObject().getPsuIdColumns();
				List<ColumnJoin> suPsuJoinColumns = samplingDesign.getTwoStagesSettingsObject().getSamplingUnitPsuJoinColumns();
				
				for (int i = 0; i < psuJoinColumns.size(); i++) {
					ColumnJoin psuCol = psuJoinColumns.get(i);
					ColumnJoin suCol = suPsuJoinColumns.get(i);
					Field expfField = expfTable.field( psuCol.getColumn() );
					Condition join = expfField.eq( sourceTable.field(suCol.getColumn()) );
					if( conditions == null ){
						conditions = join;
					} else {
						conditions = conditions.and( join );
					}
				}
				
//				select.addJoin( aoiTable, condition );
				
				
//				for ( Field<?> psuKeyField : psuTable.getPsuFields() ) {
//					@SuppressWarnings("rawtypes")
//					Field expfField 			= expfTable.field( psuKeyField.getName() );
//					Condition psuJoinCondition 	= expfField.eq( psuKeyField );
//
//					if( conditions == null ){
//						conditions = psuJoinCondition;
//					} else {
//						conditions = conditions.and( psuJoinCondition );
//					}
//				}
			} else {
				Field<Integer> aoiField = sourceTable.getAoiIdField(aoiLevel);
				conditions = expfTable.AOI_ID.eq( aoiField ).and( expfTable.WEIGHT.gt(BigDecimal.ZERO) );
			}
			if( getWorkspace().hasStratifiedSamplingDesign() ) {
				
				conditions = conditions.and( expfTable.STRATUM.eq( sourceTable.getStratumField() ) );

				select.addSelect( sourceTable.getStratumField() );
				select.addGroupBy( sourceTable.getStratumField() );
			}
			select.addJoin( expfTable, conditions );
			select.addGroupBy( expfTable.EXPF );
			
			// add sum( quantity * expf )
			// for now quantity fields. check if it needs to be done for each variable aggregate
			for ( QuantitativeVariable var : sourceTable.getEntity().getDefaultProcessingChainQuantitativeOutputVariables() ) {
				Field<BigDecimal> quantityField = sourceTable.getQuantityField(var);				
//				Field<BigDecimal> aggregateField = quantityField.mul( expfTable.EXPF ).sum();
				
				Field<BigDecimal> aggregateField = sourceTable.getWeightField().div( expfTable.WEIGHT ).mul( expfTable.AREA ).mul( quantityField ).sum();
				
				
				select.addSelect( aggregateField.as(quantityField.getName() ) );
			}
			Field<BigDecimal> weightField = sourceTable.getWeightField();
			if( weightField != null ){
				select.addSelect( weightField.mul( expfTable.EXPF ).sum().as( weightField.getName()) );
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

	private void createSamplingUnitAggregateTable( SamplingUnitAggregateTable suAggTable ) {
//		SamplingUnitAggregateTable plotAgg = factTable.getPlotAggregateTable();
		FactTable sourceTable = (FactTable) suAggTable.getSourceTable();
		SelectQuery<Record> select = psql().selectQuery();
		
		select.addFrom( sourceTable );
		
		select.addSelect( sourceTable.getSamplingUnitIdField() );
		select.addGroupBy( sourceTable.getSamplingUnitIdField() );

		//		select.addSelect( sourceTable.getDimensionIdFields() );
		for (Field<Integer> dimField : sourceTable.getDimensionIdFields()) {
			select.addSelect( DSL.coalesce(dimField,-1).as( dimField.getName() ) );
		}
		select.addGroupBy( sourceTable.getDimensionIdFields() );
		
		select.addSelect( sourceTable.getAoiIdFields() );
		select.addGroupBy( sourceTable.getAoiIdFields() );
		
		select.addSelect( sourceTable.getStratumField() );
		select.addGroupBy( sourceTable.getStratumField() );

		if( getWorkspace().hasClusterSamplingDesign() ){
			select.addSelect( sourceTable.getClusterField() );
			select.addGroupBy( sourceTable.getClusterField() );
		}
		
		if( getWorkspace().has2StagesSamplingDesign() ){
			
			SamplingDesign samplingDesign = getWorkspace().getSamplingDesign();
			List<ColumnJoin> joinColumns = samplingDesign.getTwoStagesSettingsObject().getSamplingUnitPsuJoinColumns();
			for (ColumnJoin columnJoin : joinColumns) {
				Field field 			= sourceTable.field( columnJoin.getColumn() );
				select.addSelect( field );
				select.addGroupBy( field );
			}
		} else {
			select.addSelect( sourceTable.getWeightField() );
			select.addGroupBy( sourceTable.getWeightField() );
		}
		
		
		// for now quantity fields. check if it needs to be done for each variable aggregate
		Field<BigDecimal> plotArea = ((FactTable)sourceTable) .getPlotAreaField();
		if( plotArea == null ){
			throw new CalculationException( "Plot area script has not been defined for entity " + sourceTable.getEntity().getName()+ ". Unable to aggregate this entity at plot level" );
		}
		for ( QuantitativeVariable var : sourceTable.getEntity().getDefaultProcessingChainQuantitativeOutputVariables() ) {
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
	
	
	
	@SuppressWarnings("unchecked")
	private void createFactTable(FactTable factTable) {
		EntityDataView dataTable = factTable.getEntityView();
//		Entity entity = dataTable.getEntity();
		
		SelectQuery<?> select = new Psql().selectQuery( dataTable );
		select.addSelect( dataTable.getIdField() );
//		select.addSelect( dataTable.getParentIdField() );

		// add dimensions to select
		for (Field<Integer> field : factTable.getDimensionIdFields()) {
			select.addSelect(dataTable.field(field));
		}
		for (Field<?> field : factTable.getCategoryValueFields() ) {
			select.addSelect( dataTable.field(field) );
		}
		
		// add quantities to select
		Entity entity = factTable.getEntity();
		Collection<QuantitativeVariable> vars = entity.getOriginalQuantitativeVariables();
		for (QuantitativeVariable var : vars) {
			Field<BigDecimal> fld = dataTable.getQuantityField(var);
			select.addSelect(fld);
		}
		vars = entity.getDefaultProcessingChainQuantitativeOutputVariables();
		for (QuantitativeVariable var : vars) {
			Field<BigDecimal> fld = dataTable.getQuantityField(var);
			select.addSelect(fld);
			
			// add error columns in case at least 1 error table has been defined for the given variable
			List<ErrorTable> errorTables = factTable.getErrorTables( var );
			if( errorTables.size() > 0 ){
				ErrorTable errorTable = errorTables.get( 0 );
				
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getTotalQuantityAbsoluteError().getName() ) );
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getTotalQuantityRelativeError().getName() ) );
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getTotalQuantityVariance().getName() ) );
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getMeanQuantityAbsoluteError().getName() ) );
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getMeanQuantityRelativeError().getName() ) );
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getMeanQuantityVariance().getName() ) );
			}
		}
		
		// in case entities that need to be aggregated based on their sampling design 
		if( entity.isInSamplingUnitHierarchy() ){
			
			if( !dataTable.getEntity().isSamplingUnit() ){
				select.addSelect( dataTable.getSamplingUnitIdField() );
			}
			
			// in case of sampling unit, it adds the weight (area) measure
			Field<BigDecimal> weightField = dataTable.getWeightField();
			if( weightField != null ){
				select.addSelect( weightField );
			}
			
			// add plot area to select
			Field<BigDecimal> plotAreaField = factTable.getPlotAreaField();
			if (plotAreaField != null) {
				select.addSelect( dataTable.field(plotAreaField) );
			}
			
			// add aoi ids to fact table if it's geo referenced
			SamplingDesign samplingDesign = getWorkspace().getSamplingDesign();
			if( factTable.isGeoreferenced() ) {
				
				if( getWorkspace().has2StagesSamplingDesign() ){
					DataAoiTable aoiTable = getJob().getInputSchema().getPrimarySUAoiTable();
					select.addSelect( aoiTable.getAoiIdFields() );
					
					List<ColumnJoin> psuJoinColumns = samplingDesign.getTwoStagesSettingsObject().getPsuIdColumns();
					List<ColumnJoin> suPsuJoinColumns = samplingDesign.getTwoStagesSettingsObject().getSamplingUnitPsuJoinColumns();
					
					Condition condition = null;
					for (int i = 0; i < psuJoinColumns.size(); i++) {
						ColumnJoin psuCol = psuJoinColumns.get(i);
						ColumnJoin suCol = suPsuJoinColumns.get(i);
						Field aoiField = aoiTable.field( psuCol.getColumn() );
						Condition join = aoiField.eq( dataTable.field(suCol.getColumn()) );
						if( condition == null ){
							condition = join;
						} else {
							condition = condition.and( join );
						}
					}
					
					select.addJoin( aoiTable, condition );
					
				} else {
					
					DataAoiTable aoiTable = getJob().getInputSchema().getSamplingUnitAoiTable();
					select.addSelect( aoiTable.getAoiIdFields() );
					
					Field<Long> joinField = ( dataTable.getEntity().isSamplingUnit() ) ? dataTable.getIdField() : dataTable.getSamplingUnitIdField();
					select.addJoin(aoiTable, joinField.eq(aoiTable.getIdField()) );
				}
				
			}
			
			Field<String> clusterField = null;
			// add stratum and cluster columns to fact table based on the sampling design
			if( samplingDesign.getTwoPhases() ){
				
				// add join in case of two phase sampling
				DynamicTable<Record> phase1Table = factTable.getDataSchema().getPhase1Table();
				TableJoin phase1Join = samplingDesign.getPhase1Join();
				Condition conditions = phase1Table.getJoinConditions( dataTable, phase1Join );
				select.addJoin(phase1Table, conditions);
				
				// add stratum column
				if( getWorkspace().hasStratifiedSamplingDesign() ) {
					String stratumColumn = samplingDesign.getStratumJoin().getColumn();
					Field<Integer> stratumField = phase1Table.getIntegerField( stratumColumn ).cast(Integer.class).as( factTable.getStratumField().getName() ) ;
					select.addSelect( stratumField );
				}
				// add cluster column
				if( getWorkspace().hasClusterSamplingDesign() ) {
					String clusterColumn = samplingDesign.getClusterColumn().getColumn();
					clusterField = phase1Table.getVarcharField( clusterColumn ).as( factTable.getClusterField().getName() ) ;
				} else {
//					clusterField = 	DSL.val( "1" ).as( factTable.getClusterField().getName() );
				}
				select.addSelect( clusterField );
			} else {
				// one phase sampling
				
				if( getWorkspace().hasStratifiedSamplingDesign() ) {
					// add stratum column
					String stratumColumn = samplingDesign.getStratumJoin().getColumn();
					Field<Integer> stratumField = dataTable.field( stratumColumn ).cast(Integer.class).as( factTable.getStratumField().getName() ) ;
					select.addSelect( stratumField );
				}
				
				// add cluster column
				if( getWorkspace().hasClusterSamplingDesign() ) {
					String clusterColumn = samplingDesign.getClusterColumn().getColumn();
					clusterField = dataTable.field( clusterColumn ).cast(String.class).as( factTable.getClusterField().getName() ) ;
				} else {
//					clusterField = 	DSL.val( "1" ).as( factTable.getClusterField().getName() );
				}
				select.addSelect( clusterField );
			}
			
			if( getWorkspace().has2StagesSamplingDesign() ){
				
				List<ColumnJoin> samplingUnitPsuJoinColumns = samplingDesign.getTwoStagesSettingsObject().getSamplingUnitPsuJoinColumns();
				for( ColumnJoin columnJoin : samplingUnitPsuJoinColumns ){
					select.addSelect( dataTable.field(columnJoin.getColumn()) );
				}
			}
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
	
}

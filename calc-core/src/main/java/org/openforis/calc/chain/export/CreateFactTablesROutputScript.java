package org.openforis.calc.chain.export;

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
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.metadata.SamplingDesign.TableJoin;
import org.openforis.calc.metadata.SamplingDesign.TwoStagesSettings;
import org.openforis.calc.persistence.DBProperties;
import org.openforis.calc.psql.CreateTableStep.AsStep;
import org.openforis.calc.psql.DropTableStep;
import org.openforis.calc.psql.GrantStep.OnStep.ToStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.r.RScript;
import org.openforis.calc.schema.DataAoiTable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ErrorTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class CreateFactTablesROutputScript extends ROutputScript {

	public CreateFactTablesROutputScript( int index, Workspace  workspace , Schemas schemas , DBProperties dbProperties ) {
		super( "create-fact-tables.R", createScript(workspace , schemas , dbProperties), Type.SYSTEM, index );
	}

	private static RScript createScript(Workspace workspace, Schemas schemas , DBProperties dbProperties ) {
		RScript r 						= r();
		
		DataSchema schema = schemas.getDataSchema();
		for ( FactTable factTable : schema.getFactTables() ){
			RScript createFactTableScript = createFactTableScript( workspace, schema, factTable, dbProperties );
			r.addScript( createFactTableScript );
		}
		return r;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static RScript createFactTableScript( Workspace workspace , DataSchema schema , FactTable factTable , DBProperties dbProperties ) {
		RScript r					= r();
		Entity entity 				= factTable.getEntity();
		
		EntityDataView dataView 	= factTable.getEntityView();
		
		SelectQuery<?> select 		= new Psql().selectQuery( dataView );
		select.addSelect( dataView.getIdField() );

		// add dimensions to select
		for (Field<Integer> field : factTable.getDimensionIdFields()) {
			select.addSelect(dataView.field(field));
		}
		for (Field<?> field : factTable.getCategoryValueFields() ) {
			select.addSelect( dataView.field(field) );
		}
		for (Field<?> field : factTable.getSpeciesDimensionFields() ) {
			select.addSelect( dataView.field(field) );
		}
		// add quantities to select
		Collection<QuantitativeVariable> vars = entity.getOriginalQuantitativeVariables();
		for (QuantitativeVariable var : vars) {
			Field<BigDecimal> fld = dataView.getQuantityField(var);
			select.addSelect(fld);
		}
		vars = entity.getDefaultProcessingChainQuantitativeOutputVariables();
		for (QuantitativeVariable var : vars) {
			Field<BigDecimal> fld = dataView.getQuantityField(var);
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
		if( entity.isSamplingUnit() ){
			List<ErrorTable> errorTables = factTable.getErrorTables( workspace.getAreaVariable() );
			if( errorTables.size() > 0 ){
				ErrorTable errorTable = errorTables.get( 0 );
				
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getAreaAbsoluteError().getName() ) );
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getAreaRelativeError().getName() ) );
				select.addSelect( DSL.castNull( DOUBLEPRECISION ).as( errorTable.getAreaVariance().getName() ) );
			}
		}
		
		// in case entities that need to be aggregated based on their sampling design 
		if( entity.isInSamplingUnitHierarchy() ){
			
			if( !dataView.getEntity().isSamplingUnit() ){
				select.addSelect( dataView.getSamplingUnitIdField() );
			}
			
			// in case of sampling unit, it adds the weight (area) measure
			Field<BigDecimal> weightField = dataView.getWeightField();
			if( weightField != null ){
				select.addSelect( weightField );
			}
			
			// add plot area to select
			Field<BigDecimal> plotAreaField = factTable.getPlotAreaField();
			if (plotAreaField != null) {
				select.addSelect( dataView.field(plotAreaField) );
			}
			
			// add aoi ids to fact table if it's geo referenced
			SamplingDesign samplingDesign = workspace.getSamplingDesign();
			if( factTable.isGeoreferenced() ) {
				
				if( workspace.has2StagesSamplingDesign() ){
					TwoStagesSettings twoStagesSettings = samplingDesign.getTwoStagesSettingsObject();
					DataAoiTable aoiTable 				= schema.getPrimarySUAoiTable();
					select.addSelect( aoiTable.getAoiIdFields() );
					
					List<ColumnJoin> psuJoinColumns 	= twoStagesSettings.getPsuIdColumns();
					List<ColumnJoin> suPsuJoinColumns 	= twoStagesSettings.getSamplingUnitPsuJoinColumns();
					
					Condition condition = null;
					for (int i = 0; i < psuJoinColumns.size(); i++) {
						ColumnJoin psuCol = psuJoinColumns.get(i);
						ColumnJoin suCol = suPsuJoinColumns.get(i);
						Field aoiField = aoiTable.field( psuCol.getColumn() );
						Condition join = aoiField.eq( dataView.field(suCol.getColumn()) );
						if( condition == null ){
							condition = join;
						} else {
							condition = condition.and( join );
						}
					}
					
					select.addJoin( aoiTable, condition );
					
				} else {
					
					DataAoiTable aoiTable = schema.getSamplingUnitAoiTable();
					select.addSelect( aoiTable.getAoiIdFields() );
					
					Field<Long> joinField = ( dataView.getEntity().isSamplingUnit() ) ? dataView.getIdField() : dataView.getSamplingUnitIdField();
					select.addJoin(aoiTable, joinField.eq(aoiTable.getIdField()) );
				}
				
			}
			
			Field<Integer> clusterField = null;
			// add stratum and cluster columns to fact table based on the sampling design
			if( samplingDesign.getTwoPhases() ){
				
				// add join in case of two phase sampling
				DynamicTable<Record> phase1Table = factTable.getDataSchema().getPhase1Table();
				TableJoin phase1Join = samplingDesign.getPhase1Join();
				Condition conditions = phase1Table.getJoinConditions( dataView, phase1Join );
				select.addJoin(phase1Table, conditions);
				
				// add stratum column
				if( workspace.hasStratifiedSamplingDesign() ) {
					String stratumColumn = samplingDesign.getStratumJoin().getColumn();
					Field<Integer> stratumField = phase1Table.getIntegerField( stratumColumn ).cast(Integer.class).as( factTable.getStratumField().getName() ) ;
					select.addSelect( stratumField );
				}
				// add cluster column
				if( workspace.hasClusterSamplingDesign() ) {
					String clusterColumn = samplingDesign.getClusterEntity().getIdColumn();
					clusterField = phase1Table.getIntegerField( clusterColumn ).as( factTable.getClusterField().getName() ) ;
				} else {
//					clusterField = 	DSL.val( "1" ).as( factTable.getClusterField().getName() );
				}
				select.addSelect( clusterField );
			} else {
				// one phase sampling
				
				if( workspace.hasStratifiedSamplingDesign() ) {
					// add stratum column
					String stratumColumn = samplingDesign.getStratumJoin().getColumn();
					Field<Integer> stratumField = dataView.field( stratumColumn ).cast(Integer.class).as( factTable.getStratumField().getName() ) ;
					select.addSelect( stratumField );
				}
				
				// add cluster column
				if( workspace.hasClusterSamplingDesign() ) {
					String clusterColumn = samplingDesign.getClusterEntity().getIdColumn();
					clusterField = dataView.field( clusterColumn ).cast(Integer.class).as( factTable.getClusterField().getName() ) ;
				} else {
//					clusterField = 	DSL.val( "1" ).as( factTable.getClusterField().getName() );
				}
				select.addSelect( clusterField );
			}
			
			if( workspace.has2StagesSamplingDesign() ){
				TwoStagesSettings twoStagesSettings = samplingDesign.getTwoStagesSettingsObject();

				List<ColumnJoin> samplingUnitPsuJoinColumns = twoStagesSettings.getSamplingUnitPsuJoinColumns();
				for( ColumnJoin columnJoin : samplingUnitPsuJoinColumns ){
					select.addSelect( dataView.field(columnJoin.getColumn()) );
				}
				
				
				Entity ssu = workspace.getEntityByOriginalId( twoStagesSettings.getSsuOriginalId() );
				select.addSelect( dataView.field(ssu.getIdColumn()).as(factTable.SSU_ID.getName()) );
			}
		}
		
		
		// drop table
		DropTableStep dropTableIfExists = psql().dropTableIfExistsLegacy( factTable );
		r.addScript( r().dbSendQuery( CONNECTION_VAR , dropTableIfExists ) );

		// create table
		AsStep createTable = psql().createTableLegacy( factTable ).as( select );
		r.addScript( r().dbSendQuery( CONNECTION_VAR , createTable ) );

		// Grant access to system user
		ToStep grant = psql().grant( Privilege.ALL ).on( factTable ).to( dbProperties.getUser() );
		r.addScript( r().dbSendQuery( CONNECTION_VAR , grant ) );
		
		return r;
	}
	

}

/**
 * 
 */
package org.openforis.calc.engine;

import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.openforis.calc.metadata.Aoi;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityAoiTable;
import org.openforis.calc.schema.ErrorTable;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.StratumDimensionTable;

/**
 * @author Mino Togna
 *
 */
public class CalculateErrorTask extends CalcRTask {

	
//	private QuantitativeVariable quantitativeVariable;
//	private Aoi aoi;
//	private CategoricalVariable<?> categoricalVariable;
//	private Schemas schemas;
	private ErrorTable errorTable;
	private RVariable connection;
	private Workspace workspace;
//
//	protected CalculateErrorTask( REnvironment rEnvironment , QuantitativeVariable quantitativeVariable , Aoi aoi , CategoricalVariable<?> categoricalVariable, Schemas schemas ){
//		super( rEnvironment , getName(quantitativeVariable, aoi, categoricalVariable) );
//		
//		this.quantitativeVariable = quantitativeVariable;
//		this.aoi = aoi;
//		this.categoricalVariable = categoricalVariable;
//		this.schemas = schemas;
//		
//		initScript();
//	}
	
	protected CalculateErrorTask( REnvironment rEnvironment, ErrorTable errorTable , RVariable connection ){
		super( rEnvironment , getName(errorTable.getQuantitativeVariable(), errorTable.getAoi(), errorTable.getCategoricalVariable()) );
		
		this.errorTable = errorTable;
		this.connection = connection;
		this.workspace = errorTable.getQuantitativeVariable().getEntity().getWorkspace();
		
		initScript();
	}

	private void initScript() {
		QuantitativeVariable quantitativeVariable = errorTable.getQuantitativeVariable();
		Aoi aoi = errorTable.getAoi();
		CategoricalVariable<?> categoricalVariable = errorTable.getCategoricalVariable();
		
		String name = getName( quantitativeVariable, aoi, categoricalVariable );

		addScript( r().rScript( "# ==================== " + name + " ====================" ) );
//		addScript( r().rScript("# " + name) );
//		addScript( r().rScript("# ==========") );
		
		RScript rScript = r().rScript( "print('"+name+"')" );
		addScript( rScript );
		
		Select<?> selectStrata = getStratumSelect( aoi, (DataSchema) this.errorTable.getSchema() );
//		Select<?> selectPlots = getPlotSelect( aoi, schema, category, classes );
//		Select<?> selectData = getDataSelect( aoi, schema, quantity, category, classes );
		
		RVariable strata = r().variable("strata");
		addScript( r().setValue( strata , r().dbGetQuery(connection, selectStrata) ) );
		
	}

	private Select<?> getStratumSelect( Aoi aoi , DataSchema schema ){
		
		StratumDimensionTable stratumTable = schema.getStratumDimensionTable();
		ExpansionFactorTable expfTable = schema.getExpansionFactorTable( aoi.getAoiLevel() );

		SelectQuery<Record> select = new Psql().selectQuery();
		
		select.addSelect( expfTable.AREA );
		select.addFrom( expfTable );
		select.addConditions( expfTable.AOI_ID.eq(aoi.getId()) );
		
		if( workspace.hasStratifiedSamplingDesign() ){
			select.addSelect( stratumTable.getStratumNo().as( "stratum") );
			
			select.addJoin( stratumTable, 
					expfTable.STRATUM.eq( stratumTable.getStratumNo() )
					.and( stratumTable.getWorkspaceId().eq( workspace.getId() ) )
					);
		} else {
			select.addSelect( DSL.val(1).as( "stratum") );
		}
	
		return select;
	}
	
	@SuppressWarnings("unchecked")
	private Select<?> getPlotSelect( Aoi aoi , DataSchema schema , CategoricalVariable<?> category ){
		Entity samplingUnit = workspace.getSamplingUnit();
		FactTable suFactTable = schema.getFactTable( samplingUnit );
		EntityAoiTable suAoiTable = schema.getEntityAoiTable( samplingUnit );
		
		SelectQuery<Record> select = new Psql().selectQuery();
		select.setDistinct(true);
		select.addSelect( suFactTable.getIdField().as("plot_id") );
		if( workspace.hasStratifiedSamplingDesign() ){
			select.addSelect( suFactTable.getStratumField().as("stratum") );
		} else {
			select.addSelect( DSL.val(1).as( "stratum") );
		}
		select.addSelect( suFactTable.getClusterField().as("cluster") );
		select.addSelect( suFactTable.field(DataTable.WEIGHT_COLUMN) );
//		Field<String> categoryField = (Field<String>) suFactTable.getCategoryValueField( category );
		Field<Integer> dimensionIdField = suFactTable.getDimensionIdField( category );
		select.addSelect( dimensionIdField.as( "class_id" ) );
//		select.addSelect( DSL.decode()
//									.when( categoryField.in(classes), 1)
//									.otherwise(0)
//									.as("class")
//							);
		
		select.addFrom( suFactTable );
		
		select.addJoin( suAoiTable, suAoiTable.getIdField().eq(suFactTable.getIdField()) );
		
		select.addConditions( suAoiTable.getAoiIdField(aoi.getAoiLevel()).eq( aoi.getId().longValue() ) );
		
		return select;
	}
	
	private static String getName( QuantitativeVariable quantitativeVariable , Aoi aoi, CategoricalVariable<?> categoricalVariable ){
		StringBuilder taskName = new StringBuilder();
		
		taskName.append( "Calculate error for " );
		taskName.append( quantitativeVariable.getName() );
		taskName.append( " " );
		taskName.append( aoi.getCaption() );
		taskName.append( " " );
		taskName.append( categoricalVariable.getName() );
		
		return taskName.toString();
	}

}

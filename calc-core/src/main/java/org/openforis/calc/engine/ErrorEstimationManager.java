/**
 * 
 */
package org.openforis.calc.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.jooq.impl.SchemaImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openforis.calc.metadata.Aoi;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.DbConnect;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityAoiTable;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.Schemas;
import org.openforis.calc.schema.StratumDimensionTable;
import org.openforis.calc.schema.TableDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Manager for error estimation
 * Now based only on "Formulas for estimators and their variances in NFI 28.2.2014 K.T. Korhonen & Olli Salmensuu, point estimators"
 * 
 * @author Mino Togna
 *
 */
@Component
public class ErrorEstimationManager {
	
	@Autowired
	private TableDataDao tableDataDao;
	
	@Autowired
	R r;
	
	@Value("${calc.jdbc.host}")
	private String host;
	@Value("${calc.jdbc.db}")
	private String database;
	@Value("${calc.jdbc.username}")
	private String user;
	@Value("${calc.jdbc.password}")
	private String password;
	@Value("${calc.jdbc.port}")
	private int port;
	
	public synchronized 
	List<DataRecord> estimateError( Workspace workspace , Aoi aoi , QuantitativeVariable quantity, CategoricalVariable<?> category, List<String> classes ) 
			throws RException {
		
		Schemas schemas = new Schemas( workspace );
		DataSchema schema = schemas.getDataSchema();

		
		Select<?> selectStrata = getStratumSelect( aoi, schema );
		Select<?> selectPlots = getPlotSelect( aoi, schema, category, classes );
		Select<?> selectData = getDataSelect( aoi, schema, quantity, category, classes );
		
		List<RScript> scripts = new ArrayList<RScript>();
		
		scripts.add( r().library( "RPostgreSQL" ) );
		scripts.add( RScript.getCalcCommonScript() );
		scripts.add( RScript.getErrorEstimationScript() );
		// create driver
		RVariable driver = r().variable("driver");
		scripts.add( r().setValue(driver, r().dbDriver("PostgreSQL")) );

		RVariable connection = r().variable("connection");
		DbConnect dbConnect = r().dbConnect(driver, host, database, user, password, port);
		scripts.add( r().setValue(connection, dbConnect) );

		// set search path to current and public schemas
		scripts.add( r().dbSendQuery(connection, new Psql().setDefaultSchemaSearchPath( schema, new SchemaImpl("public"))) );
		
		// add read data scripts
		// strata
		RVariable strata = r().variable("strata");
		scripts.add( r().setValue( strata , r().dbGetQuery(connection, selectStrata) ) );
		// plots
		RVariable plots = r().variable("plots");
		scripts.add( r().setValue( plots , r().dbGetQuery(connection, selectPlots) ) );
		// data (e.g. trees)
		RVariable data = r().variable("data");
		scripts.add( r().setValue( data , r().dbGetQuery(connection, selectData) ) );
		
		
		// calculate error
		RVariable error = r().variable( "error" );
		scripts.add( r().setValue( error , r().calculateQuantityError( data, plots, strata ) ) );
		
		DynamicTable<?> errorTable = new DynamicTable<Record>( "_error", schema.getName() );
		scripts.add( r().dbRemoveTable( connection, errorTable.getName() ) );
		scripts.add( r().dbWriteTable(connection, errorTable.getName() , error) );
		
		String script = scriptsToString( scripts );
		
//		try {
//			FileUtils.writeStringToFile( new File("/home/minotogna/error.R"), script );
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		REnvironment rEnvironment = r.newEnvironment();
		rEnvironment.eval( script );
		
		
		JSONArray tableInfo = tableDataDao.info( schema.getName() , errorTable.getName() );
		List<String> errorColumns = new ArrayList<String>();
		for (Object o : tableInfo) {
			JSONObject column = (JSONObject) o;
			String columnName = column.get( "column_name" ).toString();
			errorColumns.add( columnName );
		}
		List<DataRecord> errors = tableDataDao.selectAll( errorTable , errorColumns );
		
		
		return errors;
	}
	
	private Select<?> getStratumSelect( Aoi aoi , DataSchema schema ) {
		StratumDimensionTable stratumTable = schema.getStratumDimensionTable();
		ExpansionFactorTable expfTable = schema.getExpansionFactorTable( aoi.getAoiLevel() );

		SelectQuery<Record> select = new Psql().selectQuery();
		
		select.addSelect( stratumTable.getStratumNo().as("stratum") );
		select.addSelect( expfTable.AREA );
		
		select.addFrom( stratumTable );
		
		Workspace workspace = schema.getWorkspace();
		select.addJoin( expfTable, 
						expfTable.STRATUM.eq( stratumTable.getStratumNo() )
						.and( expfTable.AOI_ID.eq(aoi.getId()) )
						.and( stratumTable.getWorkspaceId().eq( workspace .getId() ) )
					);
	
		return select;
	}
	
	@SuppressWarnings("unchecked")
	private Select<?> getPlotSelect( Aoi aoi , DataSchema schema , CategoricalVariable<?> category , List<String> classes ) {
		Workspace workspace = schema.getWorkspace();
		Entity samplingUnit = workspace.getSamplingUnit();
		FactTable suFactTable = schema.getFactTable( samplingUnit );
		EntityAoiTable suAoiTable = schema.getEntityAoiTable( samplingUnit );
		
		SelectQuery<Record> select = new Psql().selectQuery();
		select.setDistinct(true);
		select.addSelect( suFactTable.getIdField().as("plot_id") );
		select.addSelect( suFactTable.getStratumField().as("stratum") );
		select.addSelect( suFactTable.getClusterField().as("cluster") );
		select.addSelect( suFactTable.field(DataTable.WEIGHT_COLUMN) );
		Field<String> categoryField = (Field<String>) suFactTable.getCategoryValueField( category );
		select.addSelect( DSL.decode()
									.when( categoryField.in(classes), 1)
									.otherwise(0)
									.as("class")
							);
		
		select.addFrom( suFactTable );
		
		select.addJoin( suAoiTable, suAoiTable.getIdField().eq(suFactTable.getIdField()) );
		
		select.addConditions( suAoiTable.getAoiIdField(aoi.getAoiLevel()).eq( aoi.getId().longValue() ) );
		
		return select;
	}
	
	@SuppressWarnings("unchecked")
	private Select<?> getDataSelect( Aoi aoi , DataSchema schema , QuantitativeVariable quantity , CategoricalVariable<?> category , List<String> classes ) {
		Workspace workspace = schema.getWorkspace();
		Entity samplingUnit = workspace.getSamplingUnit();
		Entity entity = quantity.getEntity();
		FactTable factTable = schema.getFactTable(entity);
		EntityAoiTable suAoiTable = schema.getEntityAoiTable( samplingUnit );
		
		SelectQuery<Record> select = new Psql().selectQuery();
		select.setDistinct(true);
		
		select.addSelect( factTable.getParentIdField().as("plot_id") );
		select.addGroupBy( factTable.getParentIdField() );
		
		select.addSelect( factTable.getStratumField().as("stratum") );
		select.addGroupBy( factTable.getStratumField() );
		
		if( workspace.hasClusterSamplingDesign() ){
			select.addSelect( factTable.getClusterField().as("cluster") );
			select.addGroupBy( factTable.getClusterField() );
			
		}
		
		Field<BigDecimal> plotAreaField = factTable.getPlotAreaField();
		select.addSelect( DSL.sum( factTable.getQuantityField(quantity).div(plotAreaField) ).as("quantity") );
		
		Field<String> categoryField = (Field<String>) factTable.getCategoryValueField( category );
		select.addSelect( DSL.decode()
									.when( categoryField.in(classes), 1 )
									.otherwise( 0 )
									.as( "class" )
							);
		select.addGroupBy( categoryField );
		
		select.addFrom( factTable );
		select.addJoin( suAoiTable, suAoiTable.getIdField().eq( factTable.getParentIdField() ) );
		select.addConditions( suAoiTable.getAoiIdField(aoi.getAoiLevel()).eq( aoi.getId().longValue() ) );
		
		return select;
	}
	
	protected RScript r() {
		return new RScript();
	}
	
	protected String scriptsToString( List<RScript> scripts ) {
		StringBuilder sb = new StringBuilder();
		for ( RScript script : scripts ) {
			String scriptString = script.toString();
			sb.append(scriptString);
		}
		return sb.toString();
	}
}

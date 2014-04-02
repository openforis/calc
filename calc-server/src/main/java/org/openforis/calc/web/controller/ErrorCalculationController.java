/**
 * 
 */
package org.openforis.calc.web.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.jooq.impl.SchemaImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.SessionManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.metadata.Aoi;
import org.openforis.calc.metadata.AoiHierarchy;
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
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.Schemas;
import org.openforis.calc.schema.StratumDimensionTable;
import org.openforis.calc.schema.TableDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

/**
 * Error calculation controller
 * 
 * @author Mino Togna
 * 
 */
@Controller
@Scope( WebApplicationContext.SCOPE_SESSION )
@RequestMapping(value = "/rest/error")
public class ErrorCalculationController {
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private CalculationStepDao calculationStepDao;

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
	
	@RequestMapping(value = "/execute.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	@SuppressWarnings("unchecked")
	synchronized Object execute(@RequestParam String arguments) throws InvalidProcessingChainException, WorkspaceLockedException, ParseException, RException {
		Workspace workspace = sessionManager.getWorkspace();

		ParameterHashMap parameterMap = new ParameterHashMap( (JSONObject) new JSONParser().parse( arguments ) );
		
		// { "classes":["101","102","103","104"],"aoi":1000,"category":292, "quantity":316 }
		int aoiId = parameterMap.getInteger( "aoi" );
		AoiHierarchy aoiHierarchy = workspace.getAoiHierarchies().get(0);
		Aoi aoi = aoiHierarchy.getAoiById( aoiId );
		
		int quantityId = parameterMap.getInteger( "quantity" );
		QuantitativeVariable quantity = (QuantitativeVariable) workspace.getVariableById( quantityId );
		Entity entity = quantity.getEntity();
		
		int categoryId = parameterMap.getInteger( "category" );
		CategoricalVariable<?> category = (CategoricalVariable<?>) entity.findVariable( categoryId );
		
		List<String> classes = (List<String>) parameterMap.get( "classes" );
		
		Schemas schemas = new Schemas( workspace );
		InputSchema schema = schemas.getInputSchema();
		FactTable factTable = schema.getFactTable(entity);
		
		// select strata
		StratumDimensionTable stratumTable = schema.getStratumDimensionTable();
		ExpansionFactorTable expfTable = schema.getExpansionFactorTable( aoi.getAoiLevel() );
		
		SelectQuery<Record> selectStrata = new Psql().selectQuery();
		selectStrata.addSelect( stratumTable.getStratumNo().as("stratum") );
		selectStrata.addSelect( expfTable.AREA );
		selectStrata.addFrom( stratumTable );
		selectStrata.addJoin( expfTable, 
						expfTable.STRATUM.eq(stratumTable.getStratumNo())
						.and(expfTable.AOI_ID.eq(aoiId))
						.and(stratumTable.getWorkspaceId().eq(workspace.getId()))
					);
		
		
		// select plots
		Entity samplingUnit = workspace.getSamplingUnit();
		FactTable suFactTable = schema.getFactTable( samplingUnit );
		EntityAoiTable suAoiTable = schema.getEntityAoiTable(samplingUnit);
		
		SelectQuery<Record> selectPlots = new Psql().selectQuery();
		selectPlots.setDistinct(true);
		selectPlots.addSelect( suFactTable.getIdField().as("plot_id") );
		selectPlots.addSelect( suFactTable.getStratumField().as("stratum") );
		if( workspace.hasClusterSamplingDesign() ){
			selectPlots.addSelect( suFactTable.getClusterField().as("cluster") );
		}
		selectPlots.addSelect( suFactTable.field(DataTable.WEIGHT_COLUMN) );
		
		Field<String> categoryField = (Field<String>) suFactTable.getCategoryValueField( category );
		selectPlots.addSelect( DSL.decode()
									.when( categoryField.in(classes), 1)
									.otherwise(0)
									.as("class")
							);
		
		selectPlots.addFrom( suFactTable );
		selectPlots.addJoin( suAoiTable, suAoiTable.getIdField().eq(suFactTable.getIdField()) );
		selectPlots.addConditions( suAoiTable.getAoiIdField(aoi.getAoiLevel()).eq( (long) aoiId) );
		
		
		
		SelectQuery<Record> selectData = new Psql().selectQuery();
		selectData.setDistinct(true);
		
		selectData.addSelect( factTable.getParentIdField().as("plot_id") );
		selectData.addGroupBy( factTable.getParentIdField() );
		
		selectData.addSelect( factTable.getStratumField().as("stratum") );
		selectData.addGroupBy( factTable.getStratumField() );
		
		if( workspace.hasClusterSamplingDesign() ){
			selectData.addSelect( factTable.getClusterField().as("cluster") );
			selectData.addGroupBy( factTable.getClusterField() );
			
		}
		
		Field<BigDecimal> plotAreaField = factTable.getPlotAreaField();
		selectData.addSelect( DSL.sum( factTable.getQuantityField(quantity).div(plotAreaField) ).as("quantity") );
		
		Field<String> fCategoryField = (Field<String>) factTable.getCategoryValueField( category );
		selectData.addSelect( DSL.decode()
									.when( fCategoryField.in(classes), 1 )
									.otherwise( 0 )
									.as( "class" )
							);
		selectData.addGroupBy( fCategoryField );
		
		selectData.addFrom( factTable );
		selectData.addJoin( suAoiTable, suAoiTable.getIdField().eq( factTable.getParentIdField() ) );
		selectData.addConditions( suAoiTable.getAoiIdField(aoi.getAoiLevel()).eq( (long) aoiId) );
		
		
		List<RScript> scripts = new ArrayList<RScript>();
		
		scripts.add( r().library( "RPostgreSQL" ) );
		scripts.add( RScript.getCalcRScript() );
		scripts.add( RScript.getErrorEstimation() );
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
		// qtyErr <- calculateQuantityError( data=data, plots=plots, strata=strata );
		RVariable error = r().variable( "error" );
		scripts.add( r().setValue( error , r().calculateQuantityError( data, plots, strata ) ) );
		
		DynamicTable<?> errorTable = new DynamicTable<Record>( "_error", schema.getName() );
		scripts.add( r().dbRemoveTable( connection, errorTable.getName() ) );
		scripts.add( r().dbWriteTable(connection, errorTable.getName() , error) );
		
		String script = scriptsToString( scripts );
		
		try {
			FileUtils.writeStringToFile( new File("/home/minotogna/error.R"), script );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	
//	public static void main(string[] args) {
//		string a = "select distinct \"laputa\".\"_plot_fact\".\"_plot_id\" as \"plot_id\", \"laputa\".\"_plot_fact\".\"_stratum\" as \"stratum\", \"laputa\".\"_plot_fact\".\"_cluster\" as \"cluster\", \"laputa\".\"_plot_fact\".\"weight\", case when \"laputa\".\"_plot_fact\".\"vegetation_type\" in ('101', '102', '103', '104') then 1 else 0 end as \"class\" from \"laputa\".\"_plot_fact\" join \"laputa\".\"_plot_aoi\" on \"laputa\".\"_plot_aoi\".\"id\" = \"laputa\".\"_plot_fact\".\"_plot_id\" where \"laputa\".\"_plot_aoi\".\"_administrative_unit_level_1_id\" = 1000";
//		a = a.replaceall("'", "\\\\'");
//		system.out.println(a);
//	}
}

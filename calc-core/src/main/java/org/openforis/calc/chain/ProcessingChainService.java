/**
 * 
 */
package org.openforis.calc.chain;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openforis.calc.Calc;
import org.openforis.calc.chain.export.BaseUnitWeightROutputScript;
import org.openforis.calc.chain.export.CalcROutputScript;
import org.openforis.calc.chain.export.CalculateExpansionFactorROutputScript;
import org.openforis.calc.chain.export.CalculationStepROutputScript;
import org.openforis.calc.chain.export.CalculationStepsGroup;
import org.openforis.calc.chain.export.CalculationStepsPostExecutionROutputScript;
import org.openforis.calc.chain.export.CloseChainROutputScript;
import org.openforis.calc.chain.export.CommonROutputScript;
import org.openforis.calc.chain.export.CreateErrorTablesROutputScript;
import org.openforis.calc.chain.export.CreateFactTablesROutputScript;
import org.openforis.calc.chain.export.EntityPlotAreaROutputScript;
import org.openforis.calc.chain.export.ErrorExecutionROutputScript;
import org.openforis.calc.chain.export.ErrorFunctionsROutputScript;
import org.openforis.calc.chain.export.InitChainROutputScript;
import org.openforis.calc.chain.export.PersistResultsROutputScript;
import org.openforis.calc.chain.export.PersistUserScriptsROutputScript;
import org.openforis.calc.chain.export.ROutputScript;
import org.openforis.calc.chain.export.ReadDataROutputScript;
import org.openforis.calc.chain.export.ResetResultsROutputScript;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EntityManager;
import org.openforis.calc.metadata.ErrorSettings;
import org.openforis.calc.metadata.MetadataManager;
import org.openforis.calc.persistence.DBProperties;
import org.openforis.calc.r.RScript;
import org.openforis.calc.schema.Schemas;
import org.openforis.calc.schema.TableDao;
import org.openforis.calc.utils.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * 
 * @author Mino Togna
 * 
 */
@Service
public class ProcessingChainService {

	private static final String YYYY_MM_DD = "YYYYMMdd-HHmmss";
	
	@Autowired
	private Calc calc;
	
	@Autowired
	private ObjectMapper jsonObjectMapper;

	@Autowired
	private MetadataManager metadataManager;

	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private CategoryManager categoryManager;
	
	@Autowired
	private TableDao tableDao;
	
	@Autowired
	private TaskManager taskManager;

	@Autowired
	private DBProperties dbProperties;
	
	public ProcessingChainService() {
	}

	public List<ROutputScript> createROutputScripts( Workspace workspace ){
		Counter counter 			= new Counter();
		
		Schemas schemas 			= workspace.schemas();
		CalculationStepsGroup group = new CalculationStepsGroup( workspace );
		
		List<ROutputScript> scripts = new ArrayList<ROutputScript>();
		
		// main calc script : calc.R
		CalcROutputScript calc 		= new CalcROutputScript();
		addScript( calc  , scripts , null );
		
		// init script
		InitChainROutputScript init 		= new InitChainROutputScript( counter.increment() , dbProperties, schemas );
		addScript( init , scripts , calc );
		
		// reset results
		ResetResultsROutputScript resetResults = new ResetResultsROutputScript( counter.increment() , workspace, schemas , entityManager );
		addScript( resetResults, scripts, calc );
		
		// read data
		ReadDataROutputScript read 	= new ReadDataROutputScript( counter.increment() , group, schemas );
		addScript( read , scripts , calc );
		
		// ==================
		// USER SCRIPTS
		// ==================
		
		// Scripts that need to be persisted 
		CommonROutputScript commonScript 					= null; 
		BaseUnitWeightROutputScript weightScript 			= null;
		List<EntityPlotAreaROutputScript> plotAreaScripts	= new ArrayList<EntityPlotAreaROutputScript>();
		List<CalculationStepROutputScript> chainScripts		= new ArrayList<CalculationStepROutputScript>();
		ErrorFunctionsROutputScript errorFunctionsScript	= null;
		
		// common script
		commonScript = new CommonROutputScript( counter.increment() , workspace ); 
		addScript( commonScript , scripts , calc );
		
		if( workspace.hasSamplingDesign() ){

			// base unit weight script
			weightScript = new BaseUnitWeightROutputScript( counter.increment()  , workspace );
			addScript( weightScript, scripts, calc );
		
			// plot areas
			for (Integer entityId : group.activeEntityIds()) {
				Entity entity = workspace.getEntityById( entityId );
				if( entity.isInSamplingUnitHierarchy() && !entity.isSamplingUnit() ){
					EntityPlotAreaROutputScript plotArea = new EntityPlotAreaROutputScript( counter.increment() , entity );
					addScript(plotArea, scripts, calc);
					
					plotAreaScripts.add( plotArea );
				}
			}
		
		}
		
		RScript stepsPostExecScript = new RScript();
		// calculation steps
//		for (Integer entityId : group.activeEntityIds()) {
//			for (CalculationStep calculationStep : group.getCalculationSteps( entityId )) {
		for (CalculationStep calculationStep : group.getAllCalculationSteps() ){
			CalculationStepROutputScript step = new CalculationStepROutputScript( counter.increment() , calculationStep, schemas );
			addScript( step, scripts, calc );
			
			chainScripts.add( step );
			
			if( calculationStep.getActive() ){
				stepsPostExecScript.addScript( step.getPostExecScript() );
			}
			
		}
			
//		}
		// ==================
		// END USER SCRIPTS
		// ==================
		// execute post script calculation steps
		CalculationStepsPostExecutionROutputScript stepsPostExec = new CalculationStepsPostExecutionROutputScript( counter.increment(), stepsPostExecScript  );
		addScript(stepsPostExec, scripts, calc);
		
		// persist results
		PersistResultsROutputScript persistResults				= new PersistResultsROutputScript( counter.increment(), group, schemas, entityManager );
		addScript( persistResults, scripts, calc );
		
		// create fact tables 
		CreateFactTablesROutputScript createFactTables 				= new  CreateFactTablesROutputScript( counter.increment(), workspace, schemas, dbProperties );
		addScript( createFactTables, scripts, calc );
		
		if( workspace.hasSamplingDesign() ){
			// calculate expansion factor
			CalculateExpansionFactorROutputScript expansionFactor 	= new CalculateExpansionFactorROutputScript( counter.increment(), workspace, schemas );
			addScript( expansionFactor, scripts, calc );

			// error execution
			CreateErrorTablesROutputScript createErrorTables 	= new CreateErrorTablesROutputScript( counter.increment(), workspace, schemas );
			addScript( createErrorTables, scripts, calc );

			// error functions
			ErrorSettings errorSettings = workspace.getErrorSettings();
			errorFunctionsScript = new ErrorFunctionsROutputScript( counter.increment() , errorSettings );
			addScript( errorFunctionsScript, scripts, calc );

			ErrorExecutionROutputScript errorExec 				= new ErrorExecutionROutputScript( counter.increment() , errorSettings , schemas );
			addScript( errorExec, scripts, calc );
		}
		
		// persist user scripts
		PersistUserScriptsROutputScript persistUserScripts		= new PersistUserScriptsROutputScript( counter.increment(), schemas , commonScript , weightScript , plotAreaScripts , chainScripts , errorFunctionsScript);
		addScript( persistUserScripts, scripts, calc );
		
		// close
		CloseChainROutputScript closeChain 						= new CloseChainROutputScript( counter.increment(), workspace, schemas , entityManager );
		addScript( closeChain, scripts, calc );
		
		return scripts;
	}
	
	private void addScript(ROutputScript script , List<ROutputScript> scripts , CalcROutputScript calc ){
		scripts.add(script);
		if( calc != null ){
			calc.addScript(script);
		}
	}
	
	/**
	 * Export the given workspace to an output stream as zip file
	 * @param workspace
	 * @param outputStream
	 * @throws IOException
	 */
	public void exportToStream(  Workspace ws , List<ROutputScript> scripts , OutputStream outputStream ) throws IOException{
		ZipOutputStream stream = new ZipOutputStream( outputStream );
		String folder = "calc-" + ws.getName() + "-processing-chain-" + new SimpleDateFormat( YYYY_MM_DD ).format( new Date() );
		
		for ( ROutputScript script : scripts ) {
			addZipEntry( folder , script , stream );
		}
		
		stream.close();
	}
	
	private void addZipEntry( String folder, ROutputScript script , ZipOutputStream stream ) throws IOException {
		String value	= script.getRScript().toString() ; 
		String name 	= folder + "/" + script.getFileName();
		
		ZipEntry info 	= new ZipEntry( name );
		stream.putNextEntry(info);
		stream.write(  value.getBytes() );
		stream.closeEntry();
	}
	
	
}

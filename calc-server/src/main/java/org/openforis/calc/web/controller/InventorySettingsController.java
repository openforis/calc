package org.openforis.calc.web.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Equation;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.EquationManager;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesignManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Rest controller for the settings section
 * 
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/workspace/active")
public class InventorySettingsController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@Autowired
	private EquationManager equationManager;
	
	@Autowired
	private TaskManager taskManager;

	@RequestMapping(value = "/samplingDesign.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Response setSamplingDesign(@RequestParam(value = "samplingDesign", required = false) String samplingDesignParam) throws IOException, ParseException, WorkspaceLockedException {
		Response response = new Response();
		Workspace workspace = workspaceService.getActiveWorkspace();

		samplingDesignManager.deleteSamplingDesign( workspace );

		SamplingDesign samplingDesign = parseSamplingDesignFromJsonString( workspace, samplingDesignParam );
		if ( samplingDesign != null ) {
			samplingDesignManager.insert( workspace , samplingDesign );
			response.addField( "samplingDesign",  workspace.getSamplingDesign() );
		}

		// execute job
		Job job = taskManager.createPreProcessingJob( workspace );
		taskManager.startJob(job);
		response.addField("job", job);
		
		return response;
	}
	
	@RequestMapping(value = "/settings/equationList.json", method = RequestMethod.PUT, produces = "application/json")
	public @ResponseBody 
	Response createEquationList( @RequestParam String filePath , @RequestParam String listName ) throws IOException {
		Response response = new Response();
		
		Workspace workspace = workspaceService.getActiveWorkspace();
		validateEquationList( response, workspace , listName, null );
		if( !response.hasErrors() ) {
			equationManager.createFromCsv( workspace, filePath, listName );
			response.addField("equationLists", workspace.getEquationLists());
		}
		
		return response;
	}
	
	@RequestMapping(value = "/settings/equationList/{listId}.json", method = RequestMethod.PUT, produces = "application/json")
	public @ResponseBody 
	Response updateEquationList( @PathVariable long listId,  @RequestParam String filePath , @RequestParam String listName ) throws IOException {
		Response response = new Response();
		Workspace workspace = workspaceService.getActiveWorkspace();
		validateEquationList( response, workspace , listName, listId );
		if( !response.hasErrors() ) {
			equationManager.updateFromCsv(workspace, filePath, listName, listId);
			response.addField("equationLists", workspace.getEquationLists());
		}
		
		return response;
	} 
	
	private void validateEquationList( Response response , Workspace workspace, String listName , Long listId ){
		boolean unique = equationManager.isNameUnique( workspace, listName, listId );
		if( unique ) {
			response.setStatusOk();
		} else {
			response.setStatusError();
			ObjectError objectError = new ObjectError( "name", "Name already defined");
			response.addError( objectError );
		}
		
	}
	
	@RequestMapping(value = "/settings/equationList/{listId}/equations.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody 
	Response getEquations( @PathVariable long listId ) throws IOException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		EquationList equationList = workspace.getEquationListById( listId );
		List<Equation> equations = equationList.getEquations();
		
		Response response = new Response();
		response.addField( "equations", equations );
		
		return response;
	} 
	
	/**
	 * Parse the json object into a samplingDesing instance
	 * 
	 * @param workspace
	 * @param samplingDesignParam
	 * @return
	 * @throws ParseException
	 */
	private SamplingDesign parseSamplingDesignFromJsonString(Workspace workspace, String samplingDesignParam) throws ParseException {
		if (StringUtils.isNotEmpty(samplingDesignParam)) {
			JSONObject json = (JSONObject) new JSONParser().parse( samplingDesignParam );
			Object suId = json.get( "samplingUnitId" );
			if (suId != null) {
				SamplingDesign samplingDesign = new SamplingDesign();
				Entity entity = workspace.getEntityById(Integer.valueOf(suId.toString()));

				samplingDesign.setWorkspace(workspace);
				samplingDesign.setSamplingUnit(entity);
				
				samplingDesign.setSrs(getBooleanValue(json, "srs"));
				samplingDesign.setSystematic(getBooleanValue(json, "systematic"));
				samplingDesign.setTwoPhases(getBooleanValue(json, "twoPhases"));
				samplingDesign.setTwoStages(getBooleanValue(json, "twoStages"));
				samplingDesign.setStratified(getBooleanValue(json, "stratified"));
				samplingDesign.setCluster(getBooleanValue(json, "cluster"));
					
				samplingDesign.setPhase1JoinSettings( getParameterMapValue( json , "phase1JoinSettings" ) );
				samplingDesign.setTwoStagesSettings( getParameterMapValue( json , "twoStagesSettings" ) );
				samplingDesign.setStratumJoinSettings( getParameterMapValue( json , "stratumJoinSettings" ) );
				samplingDesign.setClusterColumnSettings( getParameterMapValue( json , "clusterColumnSettings" ) );
				samplingDesign.setAoiJoinSettings( getParameterMapValue( json , "aoiJoinSettings" ) );

				samplingDesign.setSamplingUnitWeightScript( getStringValue(json, "samplingUnitWeightScript") );
				
				return samplingDesign;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private ParameterMap getParameterMapValue( JSONObject json, String property ) throws ParseException {
		ParameterMap paramMap = null;
		Object object = json.get( property );
		if( object != null) {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse( object.toString() );
			if ( obj instanceof JSONObject ) {
				paramMap = new ParameterHashMap((JSONObject) obj);
			}
		}
		
		return paramMap;
	}

	private Boolean getBooleanValue(JSONObject json, String property) {
		Object object = json.get(property);
		Boolean value = false;
		if (object != null) {
			value = (Boolean) object;
		}
		return value;
	}

	private String getStringValue(JSONObject json, String property) {
		Object object = json.get(property);
		String value = (object != null) ? object.toString() : null ;
		return value;
	}
	
}

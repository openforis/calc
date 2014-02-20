package org.openforis.calc.web.controller;

import java.util.List;

import javax.sql.DataSource;

import org.json.simple.JSONObject;
import org.openforis.calc.engine.CollectTaskService;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.schema.CategoryDimensionTable;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/workspace/active/variable")
public class VariableController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private CollectTaskService collectTaskManager;
	
	@Autowired
	private VariableDao variableDao;
	
	@Autowired
	private DataSource dataSource; 


	@RequestMapping(value = "/{variableId}/categories.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
	Response getCategories( @PathVariable int variableId , @RequestParam(required=false) Integer max ) {
		Response response = new Response();
		
		Workspace workspace = workspaceService.getActiveWorkspace();
		Variable<?> variable = workspace.getVariableById( variableId );
		if( variable instanceof MultiwayVariable ) {

			CategoryDimensionTable table = new Schemas( workspace ).getInputSchema().getCategoryDimensionTable( (MultiwayVariable) variable );
			if( table != null ){
				
				boolean readCategories = true;			
				if( max != null ) {
					long count = variableDao.countCategories( table );
					readCategories = count <= max;
				}
				
				if( readCategories ) {
					List<JSONObject> categories = variableDao.getCategories( table );
					response.addField( "categories", categories );
				}
			}
			
		} else {
			throw new IllegalArgumentException( "Unable to find multiway variable with id " + variableId );
		}
		
		return response;
	}
	
}

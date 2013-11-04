package org.openforis.calc.web.controller;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.openforis.calc.common.NamedUserObject;
import org.openforis.calc.engine.CollectTaskService;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.web.form.VariableForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/workspace")
public class WorkspaceController {

	@Autowired
	private WorkspaceService workspaceService;
		
	@Autowired
	private TaskManager taskManager;

	@Autowired
	private CollectTaskService collectTaskManager;

	@RequestMapping(value = "/active.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Workspace getActiveWorkspace() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		return workspace;
	}
	
	//TODO change rest call /active/job.json
	@RequestMapping(value = "/job.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Job getJob() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		if( workspace == null ){
			return null;
		} else {
			Job job = taskManager.getJob(workspace.getId());
			return job;
		}
	}

	/**
	 * Set the sampling unit to the workspace
	 * @return
	 */
	@RequestMapping(value = "/active/samplingDesign/samplingUnit/{entityId}.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Workspace activeWorkspaceSetSamplingUnit(@PathVariable int entityId) {
		Workspace workspace = workspaceService.setActiveWorkspaceSamplingUnit(entityId);
		return workspace;
	}
	
	@RequestMapping(value = "/active/entity/{entityId}/variable/{variableId}/aggregates/{agg}.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	QuantitativeVariable activeWorkspaceCreateVariableAggregate(@PathVariable int entityId, @PathVariable int variableId, @PathVariable String agg ) {
		Workspace workspace = getActiveWorkspace();
		QuantitativeVariable variable = workspaceService.createVariableAggregate(workspace, entityId, variableId, agg);
		return variable;
	}
	
	@RequestMapping(value = "/active/entity/{entityId}/variable/{variableId}/aggregates/{agg}.json", method = RequestMethod.DELETE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	QuantitativeVariable activeWorkspaceDeleteVariableAggregate(@PathVariable int entityId, @PathVariable int variableId, @PathVariable String agg ) {
		Workspace workspace = getActiveWorkspace();
		QuantitativeVariable variable = workspaceService.deleteVariableAggregate(workspace, entityId, variableId, agg);
		return variable;
	}
			  //rest/workspace/active/entity/"+entityId+"/variable/"+variable.id+"/variable-per-ha.json
	@RequestMapping(value = "/active/entity/{entityId}/variable/{variableId}/variable-per-ha", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	QuantitativeVariable activeWorkspaceAddVariablePerHa(@PathVariable int entityId, @PathVariable int variableId) {
		Workspace workspace = getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		QuantitativeVariable variable = entity.getQtyVariableById(variableId);
		variable = workspaceService.addVariablePerHa(variable);
		return variable;
	}
	
	@RequestMapping(value = "/active/entity/{entityId}/variable/{variableId}/variable-per-ha", method = RequestMethod.DELETE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	QuantitativeVariable activeWorkspaceDeleteVariablePerHa(@PathVariable int entityId, @PathVariable int variableId) {
		Workspace workspace = getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		QuantitativeVariable variable = entity.getQtyVariableById(variableId);
		variable = workspaceService.deleteVariablePerHa(variable);
		return variable;
	}
	
	@Deprecated
	@RequestMapping(value = "/entities.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<Entity> getEntities() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		List<Entity> entities = new ArrayList<Entity>(workspace.getEntities());
		NamedUserObject.sortByName(entities);
		return entities;
	}
	
	@Deprecated
	@RequestMapping(value = "/entities/{entityId}/qtyvariables.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<Variable<?>> getQuantitativeVariables(@PathVariable int entityId) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		List<Variable<?>> variables = new ArrayList<Variable<?>>(entity.getQuantitativeVariables());
		NamedUserObject.sortByName(variables);
		return variables;
	}

	@Deprecated
	@RequestMapping(value = "/variable/save.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response saveVariable(@Valid VariableForm form, BindingResult result) {
		Response response = validate(form, result);
		if ( ! response.hasErrors() ) {
			Workspace ws = workspaceService.getActiveWorkspace();
			Entity entity = ws.getEntityById(form.getEntityId());
			String variableName = form.getName();
			QuantitativeVariable variable = workspaceService.saveQuantitativeVariable(entity, variableName);
			response.addField("variable", variable);
		}
		return response;
	}
	
	//TODO Where is this used??
	@RequestMapping(value = "/variable/validate.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response validate(@Valid VariableForm form, BindingResult result) {
		Response response = new Response(result.getAllErrors());
		return response;
	}
	
}

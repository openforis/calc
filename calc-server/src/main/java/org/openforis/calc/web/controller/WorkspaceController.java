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

	@RequestMapping(value = "/entities.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<Entity> getEntities() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		List<Entity> entities = new ArrayList<Entity>(workspace.getEntities());
		NamedUserObject.sortByName(entities);
		return entities;
	}
	
	@RequestMapping(value = "/entities/{entityId}/variables.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<Variable<?>> getVariables(@PathVariable int entityId) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		List<Variable<?>> variables = new ArrayList<Variable<?>>(entity.getVariables());
		NamedUserObject.sortByName(variables);
		return variables;
	}

	@RequestMapping(value = "/variable/save.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response saveVariable(@Valid VariableForm form, BindingResult result) {
		Response response = validate(form, result);
		if ( ! response.hasErrors() ) {
			Workspace ws = workspaceService.getActiveWorkspace();
			Entity entity = ws.getEntityById(form.getEntityId());
			QuantitativeVariable variable = workspaceService.addNewQuantitativeVariable(entity, form.getName());
			response.addField("variable", variable);
		}
		return response;
	}
	
	@RequestMapping(value = "/variable/validate.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response validate(@Valid VariableForm form, BindingResult result) {
		Response response = new Response(result.getAllErrors());
		return response;
	}
	
}

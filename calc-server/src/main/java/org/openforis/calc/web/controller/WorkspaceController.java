package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openforis.calc.common.NamedUserObject;
import org.openforis.calc.engine.CollectTaskService;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

	@RequestMapping(value = "/job.json", method = RequestMethod.GET, produces = "application/json")
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

	@RequestMapping(value = "/entities.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	List<Entity> getEntities() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		List<Entity> entities = new ArrayList<Entity>(workspace.getEntities());
		sortByName(entities);
		return entities;
	}
	
	@RequestMapping(value = "/entities/{entityId}/variables.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	List<Variable<?>> getVariables(@PathVariable int entityId) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		List<Variable<?>> variables = new ArrayList<Variable<?>>(entity.getVariables());
		sortByName(variables);
		return variables;
	}

	private void sortByName(List<? extends NamedUserObject> objects) {
		Collections.sort(objects, new Comparator<NamedUserObject>() {
			@Override
			public int compare(NamedUserObject o1, NamedUserObject o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}

}

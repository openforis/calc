package org.openforis.calc.web.controller;

import java.util.List;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

	@RequestMapping(value = "/job.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Job getJob() {
		List<Workspace> list = workspaceService.loadAll();
		Workspace workspace = list.get(0);

		return taskManager.getJob(workspace.getId());
	}

}

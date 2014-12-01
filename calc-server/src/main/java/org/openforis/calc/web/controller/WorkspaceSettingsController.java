/**
 * 
 */
package org.openforis.calc.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.WorkspaceSettings;
import org.openforis.calc.metadata.WorkspaceSettings.VIEW_STEPS;
import org.openforis.calc.metadata.WorkspaceSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Mino Togna
 *
 */
@Controller
@RequestMapping(value = "/rest/workspace/settings")
public class WorkspaceSettingsController {
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Autowired
	private WorkspaceSettingsManager workspaceSettingsManager; 
	
	@RequestMapping(value = "/viewSteps/{viewStepsOption}.json", method = RequestMethod.PUT, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	WorkspaceSettings setViewStepsOption( @PathVariable String  viewStepsOption) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		WorkspaceSettings settings = workspace.getSettings();
		VIEW_STEPS viewSteps = VIEW_STEPS.valueOf( viewStepsOption.toUpperCase() );
		settings.setViewSteps( viewSteps  );
		
		workspaceSettingsManager.save( workspace );
		
		return settings;
	}
}

package org.openforis.calc.web.controller;

import java.io.IOException;

import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.AoiManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for the active workspace areas of interest settings
 * 
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/workspace/active/aoi")
public class AoiController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private AoiManager aoiManager;
	
	@RequestMapping(value = "/import.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Response csvFileImport(@RequestParam("filepath") String filepath, @RequestParam("captions") String[] captions) throws IOException {
		Response response = new Response();
		
		aoiManager.csvImport(workspaceService.getActiveWorkspace(), filepath, captions);
		
		return response;
	}

}

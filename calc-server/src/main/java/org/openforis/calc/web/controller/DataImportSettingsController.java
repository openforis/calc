package org.openforis.calc.web.controller;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.Stratum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
@RequestMapping(value = "/rest/workspace/active/")
public class DataImportSettingsController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private AoiManager aoiManager;
	
	@Autowired
	private TaskManager taskManager;
	
	@RequestMapping(value = "/aoi/import.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Response aoisCsvImport(@RequestParam("filepath") String filepath, @RequestParam("captions") String[] captions) throws IOException, WorkspaceLockedException {
		Response response = new Response();
		
		Workspace workspace = workspaceService.getActiveWorkspace();
		aoiManager.csvImport(workspace, filepath, captions);
		
		AoiHierarchy aoiHierarchy = workspace.getAoiHierarchies().get(0);
		response.addField( "aoiHierarchy", aoiHierarchy );
		
		if( workspace.hasSamplingDesign() ){
			Job job = taskManager.createPreProcessingJob( workspace );
			taskManager.startJob( job );
			
			response.addField( "job" , job );
		}
		
		return response;
	}

	
	@RequestMapping(value = "/strata/import.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	List<Stratum> strataCsvImport(@RequestParam("filepath") String filepath) throws IOException {
		Workspace activeWorkspace = workspaceService.getActiveWorkspace();
		workspaceService.importStrata(activeWorkspace, filepath);
		
		return activeWorkspace.getStrata();
	}

	@RequestMapping(value = "/phase1plotstable.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Workspace setPhase1PlotsTable(@RequestParam("table") String table) throws IOException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		workspace.setPhase1PlotTable(table);
		
		workspaceService.save(workspace);
		
		return workspace;
	}
}

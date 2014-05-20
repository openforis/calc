package org.openforis.calc.web.controller;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.engine.Workspace;
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
	
	@RequestMapping(value = "/aoi/import.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	AoiHierarchy aoisCsvImport(@RequestParam("filepath") String filepath, @RequestParam("captions") String[] captions) throws IOException {
		Workspace activeWorkspace = workspaceService.getActiveWorkspace();
		aoiManager.csvImport(activeWorkspace, filepath, captions);
		
		AoiHierarchy aoiHierarchy = activeWorkspace.getAoiHierarchies().get(0);
		return aoiHierarchy;
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

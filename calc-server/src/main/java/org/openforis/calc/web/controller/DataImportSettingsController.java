package org.openforis.calc.web.controller;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.AOICsvFileParser;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.ErrorSettings;
import org.openforis.calc.metadata.ErrorSettingsManager;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesignManager;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.psql.Psql;
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
	private SamplingDesignManager samplingDesignManager;
	
	@Autowired
	private TaskManager taskManager;
	
	@Autowired
	private ErrorSettingsManager errorSettingsManager;
	
	@Autowired
	private Psql psql;
	
	@RequestMapping(value = "/aoi/import.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Response aoisCsvImport(@RequestParam("filepath") String filepath, @RequestParam("levels") int levels , @RequestParam("hasStrata") boolean hasStrata , @RequestParam("captions") String[] captions) throws IOException, WorkspaceLockedException {
		Response response = new Response();
		
		Workspace workspace = workspaceService.getActiveWorkspace();
		AOICsvFileParser aoiCsvFileParser = new AOICsvFileParser(filepath , psql);
		aoiCsvFileParser.parseForImport(levels, captions, hasStrata);
//		AoiHierarchy aoiHierarchy2 = aoiCsvFileParser.getAoiHierarchy();
		
//		Aoi rootAoi = aoiHierarchy2.getRootAoi();
//		System.out.println( rootAoi.getCode() + "    " + rootAoi.getCaption() + "   " + rootAoi.getLandArea());
//		BigDecimal area = new BigDecimal(0);
//		for (Aoi aoi : rootAoi.getChildren()) {
//			BigDecimal landArea = aoi.getLandArea();
//			area = area.add(landArea);
//			System.out.println( "\t" +aoi.getCode() + "    " + aoi.getCaption()  + "   " + landArea);
//		}
//		System.out.println( area );
//		
//		Collection<StratumAoi> strataAois = aoiCsvFileParser.getStrataAois();
//		Double total = 0.0 ;
//		for (StratumAoi stratumAoi : strataAois) {
//			if( stratumAoi.getAoi().getAoiLevel().getRank() == 0 ){
//				Double a = stratumAoi.getArea();
//				total += a;
//			}
//			System.out.println( stratumAoi.getAoi().getCaption() + "    " + stratumAoi.getStratum().getStratumNo() + "     area: " + stratumAoi.getArea() );
//		}
//		DecimalFormat f = new DecimalFormat("###,###.#####");
//		System.out.println( f.format(total) );
		
		// insert aois
		SamplingDesign samplingDesign = workspace.getSamplingDesign();
		samplingDesignManager.deleteStrataAois(workspace);
		aoiManager.insert( workspace , aoiCsvFileParser.getAoiHierarchy() );
		
		if( hasStrata ){
			samplingDesignManager.setStrata(workspace, aoiCsvFileParser.getStrata());
			
			samplingDesignManager.setStrataAois(workspace, aoiCsvFileParser.getStrataAois());
			
			if( samplingDesign == null ){
				samplingDesign = new SamplingDesign();
			}
			samplingDesign.setStratumAoi(true);
			samplingDesignManager.save(workspace, samplingDesign);
			
			response.setWorkspaceChanged();
		} else {
			// if sampling desgin != null and previously set stratumAoi=true, it sets it to false
			samplingDesign.setStratumAoi(false);
			samplingDesignManager.save(workspace, samplingDesign);
			
			response.setWorkspaceChanged();
		}
		
		AoiHierarchy aoiHierarchy = workspace.getAoiHierarchies().get(0);
		response.addField( "aoiHierarchy", aoiHierarchy );
		
		ErrorSettings errorSettings = workspace.getErrorSettings();
		if( errorSettings != null ){
			errorSettings.resetParameters();
			errorSettingsManager.save( workspace );
			
			response.addField( "errorSettings" , errorSettings );
		}
		
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

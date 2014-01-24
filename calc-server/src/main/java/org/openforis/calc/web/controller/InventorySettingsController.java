package org.openforis.calc.web.controller;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.SamplingDesignDao;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
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
@RequestMapping(value = "/rest/workspace/active")
public class InventorySettingsController {

	@Autowired
	private WorkspaceService workspaceService;
//
//	@Autowired
//	private AoiManager aoiManager;
	
	@Autowired
	private SamplingDesignDao samplingDesignDao;
	
	@RequestMapping(value = "/samplingDesign.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	SamplingDesign setSamplingDesign(@RequestParam("samplingDesign") String samplingDesignParam) throws IOException, ParseException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		SamplingDesign samplingDesign = parseSamplingDesignFromJsonString( workspace, samplingDesignParam );
		
		samplingDesignDao.save(samplingDesign);
		workspace.setSamplingDesign(samplingDesign);
		
		return samplingDesign;
	}

	private SamplingDesign parseSamplingDesignFromJsonString( Workspace workspace, String samplingDesignParam) throws ParseException {
		
		JSONObject json = (JSONObject) new JSONParser().parse(samplingDesignParam);
		SamplingDesign samplingDesign = new SamplingDesign();
		Object suId = json.get("samplingUnitId");
		if( suId != null){
			
			Entity entity = workspace.getEntityById( ((Number) suId).intValue() );
			samplingDesign.setSamplingUnit( entity );
			
			
			
		}
		return samplingDesign;
	}

}

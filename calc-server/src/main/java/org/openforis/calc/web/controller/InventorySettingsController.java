package org.openforis.calc.web.controller;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.SamplingDesignDao;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
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
	// @Autowired
	// private AoiManager aoiManager;

	@Autowired
	private SamplingDesignDao samplingDesignDao;

	@RequestMapping(value = "/samplingDesign.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Response setSamplingDesign(@RequestParam(value = "samplingDesign", required = false) String samplingDesignParam) throws IOException, ParseException {
		Response response = new Response();
		Workspace workspace = workspaceService.getActiveWorkspace();
		workspace.setSamplingDesign(null);
		samplingDesignDao.deleteByWorkspace(workspace.getId());

		SamplingDesign samplingDesign = parseSamplingDesignFromJsonString(workspace, samplingDesignParam);
		if (samplingDesign != null) {
			samplingDesignDao.save(samplingDesign);
			workspace.setSamplingDesign(samplingDesign);
			response.addField("samplingDesign", samplingDesign);
		}

		return response;
	}

	/**
	 * Parse the json object into a samplingDesing instance
	 * 
	 * @param workspace
	 * @param samplingDesignParam
	 * @return
	 * @throws ParseException
	 */
	private SamplingDesign parseSamplingDesignFromJsonString(Workspace workspace, String samplingDesignParam) throws ParseException {
		if (StringUtils.isNotEmpty(samplingDesignParam)) {
			JSONObject json = (JSONObject) new JSONParser().parse(samplingDesignParam);
			Object suId = json.get("samplingUnitId");
			if (suId != null) {
				SamplingDesign samplingDesign = new SamplingDesign();
				Entity entity = workspace.getEntityById(Integer.valueOf(suId.toString()));

				samplingDesign.setWorkspace(workspace);
				samplingDesign.setSamplingUnit(entity);
				samplingDesign.setSrs(getBooleanValue(json, "srs"));
				samplingDesign.setSystematic(getBooleanValue(json, "systematic"));
				samplingDesign.setTwoPhases(getBooleanValue(json, "twoPhases"));
				samplingDesign.setStratified(getBooleanValue(json, "stratified"));
				samplingDesign.setCluster(getBooleanValue(json, "cluster"));

				Object phase1JoinSettings = json.get("phase1JoinSettings");
				if (phase1JoinSettings != null) {
					samplingDesign.setPhase1JoinSettings(phase1JoinSettings.toString());
				}

				return samplingDesign;
			}
		}
		return null;
	}

	private Boolean getBooleanValue(JSONObject json, String property) {
		Object object = json.get(property);
		Boolean value = false;
		if (object != null) {
			value = (Boolean) object;
		}
		return value;
	}

}

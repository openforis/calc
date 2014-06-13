/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.ErrorEstimationManager;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Aoi;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.r.RException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Error calculation controller
 * 
 * @author Mino Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/error")
public class ErrorCalculationController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private ErrorEstimationManager errorEstimationManager;

	@RequestMapping(value = "/execute.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	@SuppressWarnings("unchecked")
	synchronized Object execute(@RequestParam String params) throws InvalidProcessingChainException, WorkspaceLockedException, ParseException, RException {
		Workspace workspace = workspaceService.getActiveWorkspace();

		ParameterHashMap parameterMap = new ParameterHashMap((JSONObject) new JSONParser().parse(params));

		// { "classes":["101","102","103","104"],"aoi":1000,"category":292, "quantity":316 }
		int aoiId = parameterMap.getInteger("aoi");
		AoiHierarchy aoiHierarchy = workspace.getAoiHierarchies().get(0);
		Aoi aoi = aoiHierarchy.getAoiById(aoiId);

		int quantityId = parameterMap.getInteger("quantity");
		QuantitativeVariable quantity = (QuantitativeVariable) workspace.getVariableById(quantityId);
		Entity entity = quantity.getEntity();

		int categoryId = parameterMap.getInteger("category");
		CategoricalVariable<?> category = (CategoricalVariable<?>) entity.findVariableById(categoryId);

		List<String> classes = (List<String>) parameterMap.get("classes");

		List<DataRecord> error = errorEstimationManager.estimateError( workspace, aoi, quantity, category, classes );
		return error;
	}

}

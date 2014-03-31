/**
 * 
 */
package org.openforis.calc.web.controller;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
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
	private CalculationStepDao calculationStepDao;

	@RequestMapping(value = "/execute.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	@SuppressWarnings({ "unused", "unchecked" })
	synchronized Object execute(@RequestParam String arguments) throws InvalidProcessingChainException, WorkspaceLockedException, ParseException {
		Workspace workspace = workspaceService.getActiveWorkspace();

		ParameterHashMap parameterMap = new ParameterHashMap( (JSONObject) new JSONParser().parse( arguments ) );
		
		return null;
	}

}

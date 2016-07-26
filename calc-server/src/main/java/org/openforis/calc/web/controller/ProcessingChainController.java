/**
 * 
 */
package org.openforis.calc.web.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.ParseException;
import org.openforis.calc.Calc;
import org.openforis.calc.chain.ProcessingChainService;
import org.openforis.calc.chain.export.ROutputScript;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Mino Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/processing-chain")
public class ProcessingChainController {
	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private ProcessingChainService processingChainService;
	@Autowired
	private Calc calc;

	@RequestMapping(value = "/{wsName}-processing-chain.zip", method = RequestMethod.POST)
	public void downloadProcessingChain(HttpServletResponse response) throws ParseException, IOException {

		Workspace ws = workspaceService.getActiveWorkspace();

		List<ROutputScript> scripts = processingChainService.createROutputScripts(ws);

		processingChainService.exportZipToStream(ws, scripts, response.getOutputStream());
	}

	@RequestMapping(value = "/openRStudio.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response openRStudio(HttpServletRequest req) throws IOException {
		Workspace ws = workspaceService.getActiveWorkspace();

		List<ROutputScript> scripts = processingChainService.createROutputScripts(ws);

		File calcUserHome = calc.getCalcUserHomeDirectory();
		File chainDir = processingChainService.exportToDir(ws, scripts, calcUserHome, false);

		Desktop.getDesktop().open(processingChainService.getRStudioProjectFile(chainDir));

		Response response = new Response();
		response.setStatusOk();
		return response;
	}
}

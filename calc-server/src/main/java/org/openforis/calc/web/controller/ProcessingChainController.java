/**
 * 
 */
package org.openforis.calc.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.ParseException;
import org.openforis.calc.chain.ProcessingChainService;
import org.openforis.calc.chain.export.ROutputScript;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
	

	@RequestMapping(value = "/{wsName}-processing-chain.zip", method = RequestMethod.POST )
	public void downloadProcessingChain(HttpServletResponse response) throws ParseException, IOException {
		
		Workspace ws = workspaceService.getActiveWorkspace();
		
		List<ROutputScript> scripts = processingChainService.createROutputScripts( ws );
		
		processingChainService.exportToStream( ws, scripts, response.getOutputStream() );
	}
}

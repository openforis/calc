/**
 * 
 */
package org.openforis.calc.web.controller;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Mino Togna
 * 
 */
@Controller
@RequestMapping(value = "rest/workspace/active/category")
public class CategoryController {
	@Autowired
	private WorkspaceService workspaceService;

	/**
	 * Create a new category
	 * @param name
	 * @param caption
	 * @param categoryClasses
	 * @return
	 */
	@RequestMapping(value = "create.json", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Response create(@RequestParam String name , @RequestParam String caption , @RequestParam String categoryClasses) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Response response = new Response();
		return response ;
	}
}

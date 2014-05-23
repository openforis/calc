/**
 * 
 */
package org.openforis.calc.web.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
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
@RequestMapping( value = "rest/workspace/active/category" )
public class CategoryController {
	@Autowired
	private WorkspaceService workspaceService;

	/**
	 * Create a new category
	 * @param name
	 * @param caption
	 * @param categoryClasses
	 * @return
	 * @throws ParseException 
	 */
	@RequestMapping(value = "create.json", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Response create(@RequestParam String name , @RequestParam String caption , @RequestParam String categoryClasses) throws ParseException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		Category category = new Category();
		category.setCaption(caption);
		category.setName(name);
		
		CategoryHierarchy hierarchy = new CategoryHierarchy();
		hierarchy.setCaption(caption);
		hierarchy.setName(name);
		category.addHierarchy(hierarchy);
		
		CategoryLevel level = new CategoryLevel();
		level.setName(name);
		level.setCaption(caption);
		level.setRank(1);
		hierarchy.addLevel(level);
		
		JSONArray classes =  (JSONArray) new JSONParser().parse( categoryClasses );
		for (Object o : classes) {
			JSONObject categoryClass = (JSONObject) o;
			String catCode = categoryClass.get( "code" ).toString();
			String catCaption = categoryClass.get( "category" ).toString();
		}
		
		Response response = new Response();
		return response ;
	}
}

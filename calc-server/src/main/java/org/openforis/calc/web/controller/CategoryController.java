/**
 * 
 */
package org.openforis.calc.web.controller;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.CategoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
	
	@Autowired
	private CategoryManager categoryManager;
	
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
		
		//TODO validation
		
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
		
		workspaceService.addCategory( workspace, category, classes );
		
		Response response = new Response();
		response.setStatusOk();
		response.addField( "categoryId", category.getId() );
		response.addField("categories", workspace.getCategories());
		
		return response ;
	}
	
	@RequestMapping(value = "{categoryId}/level/classes.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response getCategoryLevelClasses(@PathVariable int categoryId ){
		Workspace workspace = workspaceService.getActiveWorkspace();

		Response response = new Response();
		JSONArray categoryClasses = categoryManager.loadCategoryClasses( workspace, categoryId );
		response.addField("classes", categoryClasses);
		return response ;
	}
}

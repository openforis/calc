/**
 * 
 */
package org.openforis.calc.web.controller;

import javax.validation.Valid;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.web.controller.Response.Status;
import org.openforis.calc.web.form.CategoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	synchronized Response create(@Valid CategoryForm form, BindingResult bindingResult) {
		Response response = new Response( bindingResult.getAllErrors() );
		
		if( response.getStatus() == Status.OK ){
			
			Workspace workspace = workspaceService.getActiveWorkspace();
			
			String caption = form.getCaption();
			String name = form.getCaption().replaceAll("\\W", "_").toLowerCase();
			
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
			
			workspaceService.addCategory( workspace, category, form.getCodes() , form.getCaptions() );
			
			response.setStatusOk();
			response.addField( "categoryId", category.getId() );
			response.addField("categories", workspace.getCategories());
			
		}
		
		return response;
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

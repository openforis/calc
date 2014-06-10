/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.CategoryLevel.CategoryLevelValue;
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
			
			List<CategoryLevelValue> values = createCategoryLevelValues( form.getCodes() , form.getCaptions() );
			
			workspaceService.addCategory( workspace, category , values );
			
			response.setStatusOk();
			response.addField( "categoryId", category.getId() );
			response.addField("categories", workspace.getCategories());
			
		}
		
		return response;
	}
	
	public List<CategoryLevelValue> createCategoryLevelValues( List<String> codes , List<String> captions ) {
		boolean defaultFound = false;
		List<CategoryLevelValue> values = new ArrayList<CategoryLevel.CategoryLevelValue>();
		for ( int i = 0 ; i < codes.size() ; i++ ) {
			String catCode = codes.get(i);
			String catCaption = captions.get(i);
			
			CategoryLevelValue value = new CategoryLevelValue( (long) i, catCode, catCaption );
			values.add(value);
			
			if( catCode.equals("-1") ){
				defaultFound = true;
			}
		}
		if( !defaultFound ){
			CategoryLevelValue value = new CategoryLevelValue( -1l, "-1", "NA" );
			values.add(value);
		}
		return values;
	}
	@RequestMapping(value = "{categoryId}/level/classes.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response getCategoryDefaultLevelValues(@PathVariable int categoryId ) {
		Workspace workspace = workspaceService.getActiveWorkspace();

		Response response = new Response();
		@SuppressWarnings( "deprecation" )
		JSONArray categoryClasses = categoryManager.loadCategoryClasses( workspace, categoryId );
		response.addField("classes", categoryClasses);
		return response ;
	}
}

/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.List;

import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.schema.EntityDataViewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Rest controller for querying the data 
 * 
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/data")
public class DataController {

	@Autowired
	private WorkspaceService workspaceService;

//	@Autowired
//	private VariableDao variableDao;
//
//	@Autowired
//	private CalculationStepDao calculationStepDao;
//
//	@Autowired
//	private TaskManager taskManager;

	@Autowired
	private EntityDataViewDao entityDao;
	
	@RequestMapping(value = "/entity/{entityId}/query.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<DataRecord> query(@PathVariable int entityId, @RequestParam String fields, @RequestParam int offset, @RequestParam(value = "numberOfRows" , required=false) Integer numberOfRows, @RequestParam(required=false) Boolean excludeNull) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		
		//set limit to 5000 for the query
		if(numberOfRows==null) {
			numberOfRows = 5000;
		}
		if(excludeNull == null){
			excludeNull = false;
		}
		
		List<DataRecord> records = entityDao.query(workspace, offset, numberOfRows, entity, excludeNull, fields.split(","));
		
		return records;
	}
	
	/**
	 * Returns a {@link Response} object containing the total number of rows for the given entity
	 * @param entityId
	 * @return
	 */
	@RequestMapping(value = "/entity/{entityId}/count.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
	Response getEntityCount(@PathVariable int entityId) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		
		long count = entityDao.count(entity);
		Response response = new Response();
		response.addField("count", count);
		return response;
	}
	
//	@RequestMapping(value = "/{entityName}/query.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public @ResponseBody
//	List<DataRecord> query(@PathVariable String entityName, @RequestParam String fields, @RequestParam int offset, @RequestParam int numberOfRows) {
//		Workspace workspace = workspaceService.getActiveWorkspace();
//		
//		List<DataRecord> records = entityDao.query(workspace, offset, numberOfRows, entityName, fields.split(","));
//		
//		return records;
//	}

//	@RequestMapping(value = "/{entityId}/info.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public @ResponseBody
//	Response info(@PathVariable int entityId) {
//		Workspace workspace = workspaceService.getActiveWorkspace();
//		Entity entity = workspace.getEntityById(entityId);
//		long count = entityDao.count(entity);
//		
//		Response response = new Response();
//		response.addField("entityName", entity.getName());
//		response.addField("entityId", entityId);
//		response.addField("count", count);
//		
//		return response;
//	}
	

}

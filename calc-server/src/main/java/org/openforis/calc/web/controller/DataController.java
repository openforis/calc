/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.List;

import org.json.simple.JSONArray;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.schema.EntityDataViewDao;
import org.openforis.calc.schema.TableDataDao;
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

	@Autowired
	private EntityDataViewDao entityDao;
	
	@Autowired
	private TableDataDao tableDataDao;
	
	@RequestMapping(value = "/entity/{entityId}/query.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<DataRecord> queryByEntity(@PathVariable int entityId, @RequestParam String fields, @RequestParam int offset, @RequestParam(value = "numberOfRows" , required=false) Integer numberOfRows, @RequestParam(required=false) Boolean excludeNull) {
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
	
	
	@RequestMapping(value = "/table/info.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
	Response getTableInfo(@RequestParam String schema, @RequestParam String table) {
//		Workspace workspace = workspaceService.getActiveWorkspace();
		Response response = new Response();
		
		if( tableDataDao.exists(schema, table) ) {
			long count = tableDataDao.count(schema, table);
			response.addField("count", count);
			
			JSONArray result = tableDataDao.info(schema, table);
			response.addField("columns", result);
			
			response.addField("schema", schema);
			response.addField("table", table);
		}
		
		return response;
	} 
	
	@RequestMapping(value = "/table/query.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<DataRecord> queryByTable(@RequestParam String schema, @RequestParam String table, @RequestParam String fields, @RequestParam int offset, @RequestParam(value = "numberOfRows" , required=false) Integer numberOfRows, @RequestParam(required=false) Boolean excludeNull) {
//		Workspace workspace = workspaceService.getActiveWorkspace();
		
		//set limit to 5000 for the query
		if(numberOfRows==null) {
			numberOfRows = 5000;
		}
		if(excludeNull == null){
			excludeNull = false;
		}
		
		List<DataRecord> records = tableDataDao.query(schema, table, offset, numberOfRows, excludeNull, fields.split(","));
		
		return records;
	}
}

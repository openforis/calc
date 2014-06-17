/**
 * 
 */
package org.openforis.calc.web.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.schema.EntityDataViewDao;
import org.openforis.calc.schema.TableDao;
import org.openforis.commons.io.csv.CsvWriter;
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
 * @author S. Ricci
 * 
 */
@Controller
@RequestMapping(value = "/rest/data")
public class DataController {
	
	private static final String EXPORTED_FILE_DATE_FORMAT = "yyyy-MM-dd_HH_ss";

	private static final Log log = LogFactory.getLog(DataController.class);

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private EntityDataViewDao entityDao;
	
	@Autowired
	private TableDao tableDataDao;
	
	@RequestMapping(value = "/entity/{entityId}/query.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<DataRecord> queryByEntity(@PathVariable int entityId, @RequestParam String fields, @RequestParam int offset, @RequestParam(value = "numberOfRows" , required=false) Integer numberOfRows, @RequestParam(required=false) Boolean excludeNull, @RequestParam(required=false) String filters) throws ParseException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		
		//set limit to 5000 for the query
		if( numberOfRows == null ) {
			numberOfRows = 5000;
		}
		if( excludeNull == null ) {
			excludeNull = false;
		}
		
		JSONArray arrayFilters = filtersToJsonArray(filters);
		
		List<DataRecord> records = entityDao.query(workspace, offset, numberOfRows, entity, excludeNull, arrayFilters, fields.split(","));
		
		return records;
	}

	private JSONArray filtersToJsonArray(String filters) throws ParseException {
		JSONArray arrayFilters = null;
		if( StringUtils.isNotBlank(filters) ) {
			arrayFilters = (JSONArray) new JSONParser().parse(filters);
		}
		return arrayFilters;
	}
	
	/**
	 * Returns a {@link Response} object containing the total number of rows for the given entity
	 * @param entityId
	 * @return
	 * @throws ParseException 
	 */
	@RequestMapping(value = "/entity/{entityId}/count.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
	Response getEntityCount( @PathVariable int entityId , @RequestParam(required=false) String filters ) throws ParseException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		
		JSONArray arrayFilters = filtersToJsonArray( filters );
		
		long count = entityDao.count( entity , arrayFilters );
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
			long count = tableDataDao.count( schema, table );
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
		if( excludeNull == null ) {
			excludeNull = false;
		}
		
		List<DataRecord> records = tableDataDao.query(schema, table, offset, numberOfRows, excludeNull, fields.split(","));
		
		return records;
	}

	@RequestMapping(value = "/entity/{entityId}/data.csv", method = RequestMethod.POST)
	public void exportToCSV(HttpServletResponse response, @PathVariable int entityId, @RequestParam String fields, @RequestParam(required=false) Boolean excludeNull , @RequestParam(required=false) String filters) throws ParseException {
		String[] fieldNames = fields.split(",");
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityById(entityId);
		
		JSONArray arrayFilters = filtersToJsonArray(filters);
		
		List<DataRecord> records = entityDao.query(workspace, entity, arrayFilters, fieldNames);
		try {
			//prepare response header
			SimpleDateFormat dateFormat = new SimpleDateFormat(EXPORTED_FILE_DATE_FORMAT);
			String formattedDate = dateFormat.format(new Date());
			String fileName = String.format("%s_%s.%s", entity.getName() , formattedDate , "csv" );
			
			FileTypeMap defaultFileTypeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
			String contentType = defaultFileTypeMap.getContentType(fileName);
			response.setContentType(contentType); 
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			
			//create csv writer
			ServletOutputStream outputStream = response.getOutputStream();
			CsvWriter csvWriter = new CsvWriter(outputStream);
			csvWriter.writeHeaders(fieldNames);

			//write lines
			for (DataRecord record : records) {
				String[] line = new String[fieldNames.length];
				for (int i = 0; i < fieldNames.length; i++) {
					String field = fieldNames[i];
					Object value = record.getValue(field);
					line[i] = value == null ? null : value.toString();
				}
				csvWriter.writeNext(line);
			}
			csvWriter.close();
		} catch (IOException e) {
			log.error( "Error generating CSV file: " + e.getMessage(), e);
			throw new RuntimeException( "Error generating CSV file" , e );
		}
	}
	
}

package org.openforis.calc.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.Calc;
import org.openforis.calc.engine.CsvDataImportTask;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.engine.WorkspaceBackupService;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.ErrorSettings;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.web.form.VariableForm;
import org.openforis.calc.web.form.WorkspaceForm;
import org.openforis.commons.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author S. Ricci
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/workspace")
public class WorkspaceController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private TaskManager taskManager;
	
	@Autowired
	private DataSource dataSource; 

	@Autowired
	private Calc calc;
	
	@Autowired
	private ObjectMapper jsonObjectMapper; 
	
	@Autowired
	private WorkspaceBackupService workspaceBackupService;
	
	@RequestMapping(value = "/list.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<Workspace> getWorkspaceInfos() {
		List<Workspace> infos = workspaceService.loadAllInfos();
		return infos;
	}
	
	@RequestMapping(value = "/active.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response getActiveWorkspace() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Response response = new Response();
		response.addField("workspace", workspace);
		return response;
	}
	
	@RequestMapping(value = "/{workspaceId}/activate.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response changeActiveWorkspace( @PathVariable int workspaceId ) {
		Workspace workspace = workspaceService.get( workspaceId );
		workspaceService.activate(workspace);
		Response response = new Response();
		response.addField( "status", "OK" );
		return response;
	}
	
	@RequestMapping(value = "/{workspaceId}/delete.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response deleteWorkspace( @PathVariable int workspaceId ) throws Throwable {
		workspaceService.deleteWorkspace( workspaceId );
		
		return getActiveWorkspace();
	}
	
		
	@RequestMapping(value = "/create.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response createWorkspace(@Valid WorkspaceForm form , BindingResult result ) throws Throwable {
		
		Response response = new Response(result.getAllErrors());
		if ( !response.hasErrors() ){
			String name = form.getName();
			Workspace workspace = workspaceService.createAndActivate( name, Workspace.DEFAULT_URI, name );
			response.addField( "workspace" , workspace );
		}
		
		return response;
	}
	
	// TODO change rest call /active/job.json
	// move to JobController
	@Deprecated
	@RequestMapping(value = "/job.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Job getJob() {
		Integer activeWorkspaceId = workspaceService.getActiveWorkspaceId();
		if (activeWorkspaceId == null) {
			return null;
		} else {
			Job job = taskManager.getJob(activeWorkspaceId);
			return job;
		}
	}


	@RequestMapping(value = "/active/entity/{entityId}/variable", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response activeWorkspaceAddVariable(@PathVariable int entityId, @Valid VariableForm form, BindingResult result) {
		Response response = new Response(result.getAllErrors());
		if (!response.hasErrors()) {
			Workspace ws = workspaceService.getActiveWorkspace();
			Entity entity 		= ws.getEntityById(entityId);
			
			String variableName = form.getName();
			String type			= form.getType();
			Variable<?> variable = null;
			if( type.equalsIgnoreCase("CATEGORY") ){
				variable = workspaceService.addMultiwayVariable( entity, variableName );
			} else {
				variable = workspaceService.addQuantityVariable(entity, variableName);
			}
			response.addField("variable", variable);
			
		}
		return response;
	}

	// rest/workspace/active/entity/"+entityId+"/plot-area.json
	@RequestMapping(value = "/active/entity/{entityId}/plot-area.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Entity activeWorkspaceSetEntityPlotAreaScript(@PathVariable int entityId, @RequestParam(required=false, value="plot-area-script") String plotAreaScript) {
		Workspace workspace = workspaceService.getActiveWorkspace();

		Entity entity = workspace.getEntityById(entityId);
		entity = workspaceService.setEntityPlotAreaScript(entity, plotAreaScript);
		return entity;
	}

	@RequestMapping(value = "/active/locked.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response activeWorkspaceIsLocked() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		boolean locked = ( workspace == null ) ? false : taskManager.isLocked(workspace.getId()) ;

		Response response = new Response();
		response.addField("locked", locked);
		return response;
	}

	@RequestMapping(value = "/active/import-table.json", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	Job importTable(@RequestParam("filepath") String filepath, @RequestParam("table") String tableName, @RequestParam("columns") String columnOptions) throws ParseException,
			IOException, WorkspaceLockedException {
		
		Workspace workspace = workspaceService.getActiveWorkspace();

		Job job = taskManager.createJob(workspace);

		JSONParser jsonParser = new JSONParser();
		JSONArray array = (JSONArray) jsonParser.parse(columnOptions);
		CsvDataImportTask task = new CsvDataImportTask(filepath, tableName, array);
		job.addTask(task);

		taskManager.startJob(job);
		
		return job;
	}
	
	/**
	 * =========================
	 * Import / export methods
	 * =======================
	 */
	@RequestMapping(value = "/{wsName}-calc-workspace.zip", method = RequestMethod.POST)
	public void export(HttpServletResponse response) throws IOException {
		Workspace ws = workspaceService.getActiveWorkspace();
		WorkspaceBackup backup = workspaceBackupService.createBackup( ws );
		
		ServletOutputStream outputStream = response.getOutputStream();
		
		workspaceBackupService.exportToStream( backup, outputStream );
	}

	@RequestMapping(value = "/import.json", method = RequestMethod.POST, produces =  MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response importWs(@ModelAttribute("file") MultipartFile file) throws IOException, WorkspaceLockedException {
		Response response = new Response();
		
		// upload file
		File tempFile = File.createTempFile( "calc", "workspace.zip");
		FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
		ZipFile zipFile = new ZipFile(tempFile);
		
		WorkspaceBackup backup = workspaceBackupService.extractBackup( zipFile );
		
		Version version =  parseVersion( backup.getVersion() );
		Version calcVersion = parseVersion( calc.getVersion() );

		// if backup has been created with a newer version of calc, it fails
		if( version.compareTo( calcVersion ) <= 0 ){
			
			Workspace workspace = backup.getWorkspace();
			Workspace originalWorkspace = workspaceService.fetchByCollectSurveyUri( workspace.getCollectSurveyUri() );
			// it fails if the survey to import doesn't exist
			if( originalWorkspace == null ){
				response.setStatusError();
				response.addField( "error", "Workspace " + workspace.getName() + " not found. Unable to import." );
			} else {
				// activate first the workspace
				workspaceService.activate(originalWorkspace);
				// then it stars the import job
				Job job = workspaceBackupService.createImportBackupJob( originalWorkspace, backup );
				taskManager.startJob(job);
				
				response.setStatusOk();
				response.addField( "job", job );
			}
			
		} else {
			
			response.setStatusError();
			response.addField("error", "Backup was created using version " + version.toString()
				+ ".\n Please upgrade your Calc instance in order to proceed.\n Visit https://github.com/openforis/calc to get the download link.");
		}
		return response;
	}

	private Version parseVersion( String versionString ){
		if( versionString.equals( WorkspaceBackupService.DEV_VERSION ) ){
			versionString = "0.0";
		}
		Version version = new Version( versionString );
		return version;
	}
	
	/**
	 * =========================
	 * Error settings methods
	 * =======================
	 * @return 
	 */
	@RequestMapping( value = "/active/errorSettings.json", method = RequestMethod.PUT, produces =  MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody 
	ErrorSettings setErrorSettings( String settings ){
		if( StringUtils.isNotEmpty(settings) ){
			ErrorSettings errorSettings = new ErrorSettings( settings ); 
			Workspace workspace = workspaceService.getActiveWorkspace();
			workspaceService.setErrorSettings( workspace , errorSettings );
			
			return errorSettings;
		}
		return null;
		
	}
	
}

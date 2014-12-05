package org.openforis.calc.web.controller;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.collect.CollectBackupIdmExtractor;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author S. Ricci
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/collect")
public class CollectDataController {

	private static final String COLLECT_BACKUP_FILE_EXTENSION = "collect";
	private static final String TEMP_FILE_PREFIX = "collect";
	private static final String TEMP_FILE_SUFFIX = "metadata.zip";
	private static final String ZIP = "zip";
//	private static final String ZIP_CONTENT_TYPE = "application/zip";
//	private static final String ZIP_COMPRESSED_CONTENT_TYPE = "application/x-zip-compressed";

	private Log LOG = LogFactory.getLog(CollectDataController.class);
	
	@Autowired
	private CollectBackupIdmExtractor idmExtractor;

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private TaskManager taskManager;

	@RequestMapping(value = "/data.json", method = RequestMethod.POST, produces =  MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response importCollectData(@ModelAttribute("file") MultipartFile file, @RequestParam(required=true) int workspaceId) {
		Response response = new Response();
		
		
		if ( checkValidFile(response, file) ) {
			// upload file
			try {
				File tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
				FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
				
				// extract survey from xml file and creates it if it doesn't exist in the db
				CollectSurvey survey = idmExtractor.extractSurvey(tempFile);
				String collectSurveyUri = survey.getUri();

				Workspace workspace = workspaceService.get( workspaceId );
				String wsCollectSurveyUri = workspace.getCollectSurveyUri();
				
				// upload is allowed only to a newly created workspace (it has default uri) or to a workspace with the same survey uri
				if( wsCollectSurveyUri.equals(Workspace.DEFAULT_URI) || wsCollectSurveyUri.equals( collectSurveyUri) ) {
					
					workspace.setCollectSurveyUri(collectSurveyUri);
					
					workspaceService.activate( workspace );
					
//				Workspace ws = workspaceService.fetchByCollectSurveyUri(collectSurveyUri);
//				if (ws == null) {
//					String name = extractName(collectSurveyUri);
//					ws = workspaceService.createAndActivate(name, collectSurveyUri, name);
//				} else {
//					
//				}
					// start import job
					Job job = taskManager.createCollectSurveyImportJob( workspace, survey, tempFile );
					taskManager.startJob(job);
					
					response.addField("job", job);
				} else {
					response.setStatusError();
					response.addError( new ObjectError("fileFormat", "Unable to import.\nThe workspace " + workspace.getName() + " is linked to another collect survey.") );
				}
				
				
				
			} catch (Exception e) {
				String message = "Error extracting survey from Collect backup file";
				LOG.error(message, e);
				throw new RuntimeException(message, e);
			}
		}
		return response;
	}

	/**
	 * Returns true if the specified file is actually a ZIP file.
	 * If not, it will add an error to the Response object.
	 */
	private boolean checkValidFile(Response response, MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String extension = FilenameUtils.getExtension(fileName);
		String contentType = file.getContentType();
		System.out.println("Uploading collect backup with content type: " + contentType );
//		 ZIP_CONTENT_TYPE.equals(contentType) || ZIP_COMPRESSED_CONTENT_TYPE.equals(contentType) )
		boolean valid = ( ZIP.equalsIgnoreCase(extension) || COLLECT_BACKUP_FILE_EXTENSION.equalsIgnoreCase(extension) );
		if ( valid ) {
			return true;
		} else {
			response.setStatusError();
			response.addError(new ObjectError("fileFormat", "Only valid Collect backup file is expected.\nExpected file extensions are .zip or .collect but " + extension + " was found."));
			return false;
		}
	}

//	private String extractName(String uri) {
//		String name = uri.replaceFirst(".*/([^/?]+).*", "$1");
//		name = name.replaceAll( "\\W", "_" );
//		return name;
//	}
	
}

package org.openforis.calc.web.controller;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.openforis.calc.collect.CollectBackupIdmExtractor;
import org.openforis.calc.engine.CollectTaskService;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	private static final String TEMP_FILE_PREFIX = "collect";
	private static final String TEMP_FILE_SUFFIX = "metadata";

	@Autowired
	private CollectBackupIdmExtractor idmExtractor;

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private CollectTaskService collectTaskService;

	@Autowired
	private TaskManager taskManager;

	@RequestMapping(value = "/data.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	String importCollectData(@ModelAttribute("file") MultipartFile file) {
		try {
			
			// upload file
			File tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
			FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);

			// extract survey from xml file and creates it if it doesn't exist in the db
			CollectSurvey survey = idmExtractor.extractSurvey(tempFile);
			String surveyUri = survey.getUri();
			Workspace ws = workspaceService.fetchCollectSurveyUri(surveyUri);
			if (ws == null) {
				String name = extractName(surveyUri);
				ws = workspaceService.createAndActivate(name, surveyUri, name);
			} else {
				workspaceService.activate(ws);
			}

			// start import job
			Job job = collectTaskService.createImportJob(ws, survey, tempFile);
			taskManager.startJob(job);
			
			return Boolean.TRUE.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error while uploading file", e);
		}
	}

	private String extractName(String uri) {
		String name = uri.replaceFirst(".*/([^/?]+).*", "$1");
		return name;
	}

	// public static void main(String[] args) {
	// 
//	String url = "http://www.openforis.org/idm/naforma1";
	// url = url.replaceFirst(".*/([^/?]+).*", "$1");
	// System.out.println(url);
	// }

}

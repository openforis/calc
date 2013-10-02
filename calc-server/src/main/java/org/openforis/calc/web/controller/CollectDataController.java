package org.openforis.calc.web.controller;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openforis.calc.collect.CollectBackupIdmExtractor;
import org.openforis.calc.engine.CollectTaskManager;
import org.openforis.calc.engine.Job;
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
 * 
 * @author S. Ricci
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
	private CollectTaskManager collectTaskManager;

	@RequestMapping(value = "/data.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	String importCollectData(@ModelAttribute("file") MultipartFile file) {
		Workspace ws = loadWorkspace();
		try {
			File tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
			FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
			
			CollectSurvey survey = idmExtractor.extractSurvey(tempFile);
			
			Job job = collectTaskManager.createImportJob(ws, survey, tempFile);
			collectTaskManager.startJob(job);
			// return job;
			return Boolean.TRUE.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error while uploading file", e);
		}
	}

	private Workspace loadWorkspace() {
		List<Workspace> workspaces = workspaceService.loadAll();
		Workspace ws = workspaces.get(0);
		return ws;
	}

}

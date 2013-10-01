package org.openforis.calc.web.controller;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openforis.calc.collect.CategoriesImportTask;
import org.openforis.calc.collect.CollectBackupIdmExtractor;
import org.openforis.calc.collect.CollectDataImportTask;
import org.openforis.calc.collect.CollectInputSchemaCreatorTask;
import org.openforis.calc.collect.CollectJob;
import org.openforis.calc.collect.CollectMetadataImportTask;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	private TaskManager taskManager;
	
	@RequestMapping(value = "/data.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	String importCollectData(@ModelAttribute("file") MultipartFile file) {
		Workspace ws = loadWorkspace();
		try {
			File tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
			FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
			CollectSurvey survey = idmExtractor.extractSurvey(tempFile);
			Job job = createJob(ws, survey, tempFile);
			taskManager.startJob(job);
			//return job;
			return Boolean.TRUE.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error while uploading file", e);
		}
	}

	private Job createJob(Workspace workspace, CollectSurvey survey, File dataFile) {
		CollectJob job = taskManager.createCollectJob(workspace, survey);
		
		CollectInputSchemaCreatorTask schemaCreatorTask = taskManager.createTask(CollectInputSchemaCreatorTask.class);
		job.addTask(schemaCreatorTask);
		
		CollectMetadataImportTask importTask = taskManager.createTask(CollectMetadataImportTask.class);
		job.addTask(importTask);
		
		CategoriesImportTask categoriesImportTask = taskManager.createTask(CategoriesImportTask.class);
		job.addTask(categoriesImportTask);
		
		CollectDataImportTask dataImportTask = taskManager.createTask(CollectDataImportTask.class);
		dataImportTask.setDataFile(dataFile);
		dataImportTask.setStep(Step.ANALYSIS);
		job.addTask(dataImportTask);
		
		return job;
	}
	
	private Workspace loadWorkspace() {
		List<Workspace> workspaces = workspaceService.loadAll();
		Workspace ws = workspaces.get(0);
		return ws;
	}

}
	


package org.openforis.calc.collect;

import java.io.File;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 * 
 */
@Component
public class CollectDataService {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private TaskManager taskManager;
	
	public Job importData(CollectSurvey survey, File dataFile) throws WorkspaceLockedException {
		String surveyUri = survey.getUri();
		Workspace ws = workspaceService.fetchCollectSurveyUri(surveyUri);
		if ( ws == null ) {
			throw new IllegalArgumentException("Workspace not found for survey with URI " + surveyUri);
		}
		Job job = createJob(ws, survey, dataFile);
		taskManager.startJob(job);
		return job;
	}

	private Job createJob(Workspace workspace, CollectSurvey survey, File dataFile) {
		Job job = taskManager.createSystemJob(workspace);
		
		CollectInputSchemaCreatorTask schemaCreatorTask = taskManager.createTask(CollectInputSchemaCreatorTask.class);
		schemaCreatorTask.setSurvey(survey);
		job.addTask(schemaCreatorTask);
		
		CollectDataImportTask importTask = taskManager.createTask(CollectDataImportTask.class);
		importTask.setSurvey(survey);
		importTask.setDataFile(dataFile);
		importTask.setStep(Step.ANALYSIS);
		job.addTask(importTask);
		
		return job;
	}
	/*
	public static void main(String[] args) throws WorkspaceLockedException, BeansException, IdmlParseException {
		@SuppressWarnings("resource")
		ApplicationContext appContext = new ClassPathXmlApplicationContext(
					"/applicationContext.xml");
		CollectDataService service = appContext.getBean(CollectDataService.class);
		InputStream surveyIs = CollectDataService.class.getClassLoader().getResourceAsStream("test.idm.xml");
		CollectSurveyIdmlBinder binder = appContext.getBean(CollectSurveyIdmlBinder.class);
		CollectSurvey survey = (CollectSurvey) binder.unmarshal(surveyIs);
		URL testDataUrl = CollectDataService.class.getClassLoader().getResource("test-data.zip");
		File dataFile = new File(testDataUrl.getPath());
		service.importData(survey, dataFile);
	}
	*/
}

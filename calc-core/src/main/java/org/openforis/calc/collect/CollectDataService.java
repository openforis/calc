package org.openforis.calc.collect;

import java.io.InputStream;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
	
	public Job importData(CollectSurvey survey) throws WorkspaceLockedException {
		String surveyUri = survey.getUri();
		Workspace ws = workspaceService.fetchCollectSurveyUri(surveyUri);
		if ( ws == null ) {
			throw new IllegalArgumentException("Workspace not found for survey with URI " + surveyUri);
		}
		Job job = createJob(ws, survey);
		taskManager.startJob(job);
		return job;
	}

	private Job createJob(Workspace workspace, CollectSurvey survey) {
		Job job = taskManager.createSystemJob(workspace);
		
		CollectDataImportTask importTask = taskManager.createTask(CollectDataImportTask.class);
		importTask.setSurvey(survey);
		job.addTask(importTask);
		
		return job;
	}

	public static void main(String[] args) throws WorkspaceLockedException, BeansException, IdmlParseException {
		@SuppressWarnings("resource")
		ApplicationContext appContext = new ClassPathXmlApplicationContext(
					"/applicationContext.xml");
//		WorkspaceService workspaceService = appContext.getBean(WorkspaceService.class); 
//		Workspace ws = new Workspace();
//		ws.setName("ENF");
//		ws.setInputSchema("enf");
//		ws.setOutputSchema("calc");
//		workspaceService.save(ws);
//		int wsId = ws.getId();
		CollectDataService service = appContext.getBean(CollectDataService.class);
		InputStream surveyIs = CollectDataService.class.getClassLoader().getResourceAsStream("test.idm.xml");
		CollectSurveyIdmlBinder binder = appContext.getBean(CollectSurveyIdmlBinder.class);
		CollectSurvey survey = (CollectSurvey) binder.unmarshal(surveyIs);
		service.importData(survey);
	}
	
}

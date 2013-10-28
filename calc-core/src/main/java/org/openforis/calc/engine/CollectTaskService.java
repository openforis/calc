/**
 * 
 */
package org.openforis.calc.engine;

import java.io.File;

import org.openforis.calc.collect.CategoriesImportTask;
import org.openforis.calc.collect.CollectDataImportJob;
import org.openforis.calc.collect.CollectDataImportTask;
import org.openforis.calc.collect.CollectInputSchemaCreatorTask;
import org.openforis.calc.collect.CollectMetadataImportTask;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 * 
 */
@Component
public class CollectTaskService {

	@Autowired
	private TaskManager taskManager; 
	
	/**
	 * Create a job with write-access to the calc schema. Used for updating
	 * metadata (e.g. importing survey, code lists,..)
	 * 
	 * @param survey
	 */

	public CollectDataImportJob createImportJob(Workspace workspace, CollectSurvey survey, File dataFile) {
		CollectDataImportJob job = new CollectDataImportJob(workspace, taskManager.getDataSource(), survey);
		job.setDebugMode(taskManager.isDebugMode());

		CollectMetadataImportTask importTask = taskManager.createTask(CollectMetadataImportTask.class);
		job.addTask(importTask);

		CollectInputSchemaCreatorTask schemaCreatorTask = taskManager.createTask(CollectInputSchemaCreatorTask.class);
		job.addTask(schemaCreatorTask);

		CategoriesImportTask categoriesImportTask = taskManager.createTask(CategoriesImportTask.class);
		job.addTask(categoriesImportTask);

		CollectDataImportTask dataImportTask = taskManager.createTask(CollectDataImportTask.class);
		dataImportTask.setDataFile(dataFile);
		dataImportTask.setStep(Step.ANALYSIS);
		job.addTask(dataImportTask);

		return job;
	}

}

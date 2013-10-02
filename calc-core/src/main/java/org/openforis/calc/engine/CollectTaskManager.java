/**
 * 
 */
package org.openforis.calc.engine;

import java.io.File;

import org.openforis.calc.collect.CategoriesImportTask;
import org.openforis.calc.collect.CollectDataImportTask;
import org.openforis.calc.collect.CollectInputSchemaCreatorTask;
import org.openforis.calc.collect.CollectJob;
import org.openforis.calc.collect.CollectMetadataImportTask;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 * 
 */
@Component
public class CollectTaskManager extends TaskManager {

	/**
	 * Create a job with write-access to the calc schema. Used for updating
	 * metadata (e.g. importing survey, code lists,..)
	 * 
	 * @param survey
	 */

	public Job createImportJob(Workspace workspace, CollectSurvey survey,
			File dataFile) {
		CollectJob job = new CollectJob(workspace, getDataSource(), survey);
		job.setDebugMode(isDebugMode());

		CollectInputSchemaCreatorTask schemaCreatorTask = createTask(CollectInputSchemaCreatorTask.class);
		job.addTask(schemaCreatorTask);

		CollectMetadataImportTask importTask = createTask(CollectMetadataImportTask.class);
		job.addTask(importTask);

		CategoriesImportTask categoriesImportTask = createTask(CategoriesImportTask.class);
		job.addTask(categoriesImportTask);

		CollectDataImportTask dataImportTask = createTask(CollectDataImportTask.class);
		dataImportTask.setDataFile(dataFile);
		dataImportTask.setStep(Step.ANALYSIS);
		job.addTask(dataImportTask);

		return job;
	}

}

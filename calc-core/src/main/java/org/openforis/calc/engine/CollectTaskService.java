/**
 * 
 */
package org.openforis.calc.engine;

import java.io.File;

import org.openforis.calc.collect.CollectDataImportTask;
import org.openforis.calc.collect.CollectInputSchemaCreatorTask;
import org.openforis.calc.collect.CollectMetadataImportTask;
import org.openforis.calc.collect.SpeciesImportTask;
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

	public Job createImportJob(Workspace workspace, CollectSurvey survey, File backupFile) {
		Job job = taskManager.createCollectImportJob( workspace , survey );
		
//		job.setDebugMode(taskManager.isDebugMode());

		CollectMetadataImportTask importTask = taskManager.createTask(CollectMetadataImportTask.class);
		job.addTask(importTask);

		CollectInputSchemaCreatorTask schemaCreatorTask = taskManager.createTask(CollectInputSchemaCreatorTask.class);
		job.addTask(schemaCreatorTask);

		SpeciesImportTask speciesImportTask = taskManager.createTask(SpeciesImportTask.class);
		speciesImportTask.setBackupFile(backupFile);
		job.addTask(speciesImportTask);
		
		CollectDataImportTask dataImportTask = taskManager.createTask(CollectDataImportTask.class);
		dataImportTask.setDataFile(backupFile);
		dataImportTask.setStep(Step.ANALYSIS);
		job.addTask(dataImportTask);
		
		if( workspace.hasSamplingDesign() ) {
			taskManager.addPreProcessingTasks( job );
		}

		return job;
	}

}

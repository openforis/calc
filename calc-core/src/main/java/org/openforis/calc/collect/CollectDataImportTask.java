/**
 * 
 */
package org.openforis.calc.collect;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.jooq.Configuration;
import org.openforis.calc.engine.Task;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class CollectDataImportTask extends Task {

	private Step step;
	private File dataFile;
	
	@Autowired
	private Configuration config;
	
	@Override
	protected long countTotalItems() {
		long totalRecords;
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		CollectBackupRecordExtractor recordExtractor = null;
		try {
			recordExtractor = new CollectBackupRecordExtractor(survey, dataFile);
			recordExtractor.init();
			totalRecords = recordExtractor.countRecords();
		} catch (Exception e) {
			throw new RuntimeException("Error calculating total number of records", e);
		} finally {
			IOUtils.closeQuietly(recordExtractor);
		}
		return totalRecords;
	}
	
	@Override
	protected void execute() throws Throwable {
		
		RelationalSchema targetSchema = createInputSchema();
		
		DatabaseExporter databaseExporter = new CollectDatabaseExporter(config);
		databaseExporter.insertReferenceData(targetSchema);
		
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		CollectBackupRecordExtractor recordExtractor = null;
		try {
			recordExtractor = new CollectBackupRecordExtractor(survey, dataFile);
			recordExtractor.init();
			int recordId = 1;
			ParseRecordResult parseRecordResult = recordExtractor.nextRecord(step);
			while ( parseRecordResult != null ) {
				incrementItemsProcessed();
				if ( parseRecordResult.isSuccess()) {
					CollectRecord record = parseRecordResult.getRecord();
					record.setId(recordId++);
					databaseExporter.insertData(targetSchema, record);
				} else {
					log().error("Error importing file: " + parseRecordResult.getMessage());
				}
				parseRecordResult = recordExtractor.nextRecord(step);
			} 
		} finally {
			IOUtils.closeQuietly(recordExtractor);
		}
	}

	private RelationalSchema createInputSchema() throws CollectRdbException {
		String inputSchemaName = getWorkspace().getInputSchema();
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		RelationalSchema schema = rdbGenerator.generateSchema(survey, inputSchemaName);
		return schema;
	}
	
	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}
	
}

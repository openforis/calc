/**
 * 
 */
package org.openforis.calc.collect;

import static org.openforis.concurrency.ProgressListener.NULL_PROGRESS_LISTENER;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jooq.Configuration;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.collect.io.data.BackupDataExtractor;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.model.RelationalSchema;
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
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	public String getName() {
		return "Import data";
	}
	
	@Override
	protected long countTotalItems() {
		long totalRecords = 0;
		CollectSurvey survey = ((CollectSurveyImportJob) getJob()).getSurvey();
		BackupDataExtractor recordExtractor = null;
		try {
			ZipFile zipFile = new ZipFile(dataFile);
			recordExtractor = new BackupDataExtractor(survey, zipFile, step);
			recordExtractor.init();
			totalRecords += recordExtractor.countRecords();
		} catch (Exception e) {
			throw new RuntimeException("Error calculating total number of records", e);
		} finally {
			IOUtils.closeQuietly(recordExtractor);
		}
		return totalRecords;
	}
	
	@Override
	protected void execute() throws Throwable {
		importData();
		
		workspaceService.resetWorkspace( getWorkspace() );
	}

	private void importData() throws CollectRdbException, ZipException, IOException, Exception {
		int recordId = 1;
		CollectSurvey survey = ((CollectSurveyImportJob) getJob()).getSurvey();
		
		BackupDataExtractor recordExtractor = null;
		DatabaseExporter databaseExporter = null;
		try {
			databaseExporter = createDatabaseExporter();
			databaseExporter.insertReferenceData(NULL_PROGRESS_LISTENER);
			recordExtractor = new BackupDataExtractor(survey, dataFile, step);
			recordExtractor.init();
			ParseRecordResult parseRecordResult = recordExtractor.nextRecord();
			while ( parseRecordResult != null ) {
				incrementItemsProcessed();
				if ( parseRecordResult.isSuccess()) {
					CollectRecord record = parseRecordResult.getRecord();
					record.setId(recordId++);
					databaseExporter.insertRecordData(record, NULL_PROGRESS_LISTENER);
				} else {
					log().error("Error importing file: " + parseRecordResult.getMessage());
				}
				parseRecordResult = recordExtractor.nextRecord();
			}
		} finally {
			IOUtils.closeQuietly(recordExtractor);
			IOUtils.closeQuietly(databaseExporter);
		}
	}

	private DatabaseExporter createDatabaseExporter() {
		RelationalSchema targetSchema = ((CollectSurveyImportJob) getJob()).getInputRelationalSchema();
		return new JooqDatabaseExporter(targetSchema, config);
	}
	
//	private void createViews() {
//		workspaceService.resetDataViews(getWorkspace());
//		incrementItemsProcessed();
//	}
	
//	private void resetResults() {
//		this.workspaceService.resetCalculationResults( getWorkspace() );
//		Workspace ws = getWorkspace();
//		
//		InputSchema schema = new Schemas(ws).getInputSchema();
//		
//		List<Entity> entities = ws.getEntities();
//		for (Entity entity : entities) {
//			ResultTable resultsTable = schema.getResultTable(entity);
//			InputTable dataTable = schema.getDataTable(entity);
//			
//			if( resultsTable != null ) {
//				psql()
//					.dropTableIfExists(resultsTable)
//					.execute();
//				
//				psql()
//					.createTable(resultsTable, resultsTable.fields())
//					.execute();
//				
//				Insert<Record> insert = psql()
//					.insertInto(resultsTable, resultsTable.getIdField() )
//					.select(
//							psql()
//							.select( dataTable.getIdField() )
//							.from(dataTable)							
//						);
//				
//				insert.execute();
//			}	
//		}
//	}
	
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

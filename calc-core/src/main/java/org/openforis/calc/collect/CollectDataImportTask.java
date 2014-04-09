/**
 * 
 */
package org.openforis.calc.collect;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

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
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		BackupDataExtractor recordExtractor = null;
		try {
			recordExtractor = new BackupDataExtractor(survey, dataFile, step);
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
		RelationalSchema targetSchema = ((CollectJob) getJob()).getInputRelationalSchema();
		
		DatabaseExporter databaseExporter = new CollectDatabaseExporter(config);
		databaseExporter.insertReferenceData(targetSchema);
		
		int recordId = 1;
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		BackupDataExtractor recordExtractor = null;
		try {
			recordExtractor = new BackupDataExtractor(survey, dataFile, step);
			recordExtractor.init();
			ParseRecordResult parseRecordResult = recordExtractor.nextRecord();
			while ( parseRecordResult != null ) {
				incrementItemsProcessed();
				if ( parseRecordResult.isSuccess()) {
					CollectRecord record = parseRecordResult.getRecord();
					record.setId(recordId++);
					databaseExporter.insertData(targetSchema, record);
				} else {
					log().error("Error importing file: " + parseRecordResult.getMessage());
				}
				parseRecordResult = recordExtractor.nextRecord();
			} 
		} finally {
			IOUtils.closeQuietly(recordExtractor);
		}
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

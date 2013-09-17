/**
 * 
 */
package org.openforis.calc.collect;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jooq.Configuration;
import org.openforis.calc.engine.Task;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
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

	private CollectSurvey survey;
	private Step step;
	private File dataFile;
	
	@Autowired
	private Configuration config;
	
	@Override
	protected void execute() throws Throwable {
		
		RelationalSchema targetSchema = createInputSchema();
		
		DatabaseExporter databaseExporter = new CollectDatabaseExporter(config);
		databaseExporter.insertReferenceData(targetSchema);
		
		RecordExtractor recordExtractor = null;
		try {
			recordExtractor = new RecordExtractor(survey, dataFile);
			int recordId = 1;
			ParseRecordResult parseRecordResult = recordExtractor.nextRecord(step);
			while ( parseRecordResult != null ) {
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
		RelationalSchema schema = rdbGenerator.generateSchema(survey, inputSchemaName);
		return schema;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
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
	
	private class RecordExtractor implements Closeable {

		private static final String IDML_FILE_NAME = "idml.xml";

		//params
		private CollectSurvey survey;
		private File file;
		
		//transient
		private ZipFile zipFile;
		private DataUnmarshaller dataUnmarshaller;
		private Enumeration<? extends ZipEntry> zipEntries;

		RecordExtractor(CollectSurvey survey, File file) throws ZipException, IOException {
			this.survey = survey;
			this.file = file;
			init();
		}

		@Override
		public void close() throws IOException {
			if ( zipFile != null ) {
				zipFile.close();
			}
		}

		private void init() throws ZipException,
				IOException {
			this.dataUnmarshaller = new DataUnmarshaller(new DataHandler(survey));
			this.zipFile = new ZipFile(file);
			this.zipEntries = zipFile.entries();
		}
		
		public ParseRecordResult nextRecord(Step step) throws Exception {
			ParseRecordResult result = null;
			ZipEntry zipEntry = nextRecordEntry();
			while ( zipEntry != null ) {
				String entryName = zipEntry.getName();
				Step entryStep = extractStep(entryName);
				if ( step == null || step == entryStep ) {
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
					result = dataUnmarshaller.parse(reader);
					if ( result.isSuccess() ) {
						CollectRecord record = result.getRecord();
						record.updateRootEntityKeyValues();
						record.updateEntityCounts();
					}
					return result;
				} else {
					zipEntry = nextRecordEntry();
				}
			}
			return result;
		}
		
		public ZipEntry nextRecordEntry() {
			while ( zipEntries.hasMoreElements() ) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String entryName = zipEntry.getName();
				if ( zipEntry.isDirectory() || IDML_FILE_NAME.equals(entryName)  ) {
					continue;
				} else {
					return zipEntry;
				}
			}
			return null;
		}
		
		private Step extractStep(String zipEntryName) throws Exception {
			String[] entryNameSplitted = getEntryNameSplitted(zipEntryName);
			String stepNumStr = entryNameSplitted[0];
			int stepNumber = Integer.parseInt(stepNumStr);
			return Step.valueOf(stepNumber);
		}
		
		private String[] getEntryNameSplitted(String zipEntryName) throws Exception {
			String entryPathSeparator = Pattern.quote(File.separator);
			String[] entryNameSplitted = zipEntryName.split(entryPathSeparator);
			if (entryNameSplitted.length != 2) {
				entryPathSeparator = Pattern.quote("/");
				entryNameSplitted = zipEntryName.split(entryPathSeparator);
			}
			if (entryNameSplitted.length != 2) {
				throw new Exception("Packaged file format exception: wrong entry name: " + zipEntryName);
			}
			return entryNameSplitted;
		}
		
	}
	
}

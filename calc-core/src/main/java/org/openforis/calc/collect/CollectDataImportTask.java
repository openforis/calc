/**
 * 
 */
package org.openforis.calc.collect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.openforis.calc.engine.Task;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;

/**
 * @author S. Ricci
 *
 */
public class CollectDataImportTask extends Task {

	private static final String IDML_FILE_NAME = "idml.xml";
	
	private CollectSurvey survey;
	private Step step;
	private File dataFile;
	
	@Override
	protected void execute() throws Throwable {
		
		DataHandler handler = new DataHandler(survey);
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(handler);
		
		RecordExtractor recordExtractor = new RecordExtractor(dataUnmarshaller, dataFile);
		ParseRecordResult parseRecordResult = recordExtractor.nextRecord(step);
		while ( parseRecordResult != null ) {
			if ( parseRecordResult.isSuccess()) {
				CollectRecord parsedRecord = parseRecordResult.getRecord();
				
			}
		}
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

	
	private class RecordExtractor {
		
		//parameters
		private DataUnmarshaller dataUnmarshaller;
		
		//transient
		private ZipFile zipFile;
		private Enumeration<? extends ZipEntry> zipEntries;

		RecordExtractor(DataUnmarshaller dataUnmarshaller, File file) throws ZipException, IOException {
			this.dataUnmarshaller = dataUnmarshaller;
			this.zipFile = new ZipFile(file);
			zipEntries = zipFile.entries();
		}
		
		public ParseRecordResult nextRecord(Step step) throws Exception {
			ParseRecordResult result = null;
			ZipEntry zipEntry = nextRecordEntry();
			if ( zipEntry == null ) {
				return null;
			} else {
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
				}
			}
			return result;
		}
		
		public ZipEntry nextRecordEntry() {
			if ( zipEntries.hasMoreElements() ) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String entryName = zipEntry.getName();
				if ( zipEntry.isDirectory() || IDML_FILE_NAME.equals(entryName)  ) {
					return null;
				} else {
					return zipEntry;
				}
			} else {
				return null;
			}
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

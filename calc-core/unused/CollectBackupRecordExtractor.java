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

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;

/**
 * 
 * @author S. Ricci
 *
 */
@Deprecated
public class CollectBackupRecordExtractor implements Closeable {

	protected static final String IDML_FILE_NAME = "idml.xml";

	//params
	private CollectSurvey survey;
	protected File file;
	
	//transient
	private boolean initialized;
	protected ZipFile zipFile;
	protected Enumeration<? extends ZipEntry> zipEntries;
	private DataUnmarshaller dataUnmarshaller;

	public CollectBackupRecordExtractor(CollectSurvey survey, File file) throws ZipException, IOException {
		this.survey = survey;
		this.file = file;
		initialized = false;
	}

	public void init() throws ZipException, IOException {
		this.dataUnmarshaller = new DataUnmarshaller(new DataHandler(survey));
		this.zipFile = new ZipFile(file);
		this.zipEntries = zipFile.entries();
		initialized = true;
	}

	public ParseRecordResult nextRecord(Step step) throws Exception {
		checkInitialized();
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
	
	@Override
	public void close() throws IOException {
		if ( zipFile != null ) {
			zipFile.close();
		}
	}
	
	protected void checkInitialized() {
		if ( ! initialized ) {
			throw new IllegalStateException("Exctractor not inited");
		}
	}
	
	private Step extractStep(String zipEntryName) throws CollectBackupRecordExtractorException {
		String[] entryNameSplitted = getEntryNameSplitted(zipEntryName);
		String stepNumStr = entryNameSplitted[0];
		int stepNumber = Integer.parseInt(stepNumStr);
		return Step.valueOf(stepNumber);
	}
	
	private String[] getEntryNameSplitted(String zipEntryName) throws CollectBackupRecordExtractorException {
		String entryPathSeparator = Pattern.quote(File.separator);
		String[] entryNameSplitted = zipEntryName.split(entryPathSeparator);
		if (entryNameSplitted.length != 2) {
			entryPathSeparator = Pattern.quote("/");
			entryNameSplitted = zipEntryName.split(entryPathSeparator);
		}
		if (entryNameSplitted.length != 2) {
			throw new CollectBackupRecordExtractorException("Packaged file format exception: wrong entry name: " + zipEntryName);
		}
		return entryNameSplitted;
	}

	public long countRecords(Step step) throws CollectBackupRecordExtractorException {
		checkInitialized();
		int count = 0;
		ZipEntry entry = nextRecordEntry();
		while ( entry != null ) {
			String entryName = entry.getName();
			Step entryStep = extractStep(entryName);
			if ( step == null || step == entryStep ) {
				count++;
			}
			entry = nextRecordEntry();
		}
		return count;
	}
	
	class CollectBackupRecordExtractorException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public CollectBackupRecordExtractorException() {
		}

		public CollectBackupRecordExtractorException(String message) {
			super(message);
		}
		
	}
}

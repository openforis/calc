package org.openforis.calc.collect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CollectBackupIdmExtractor {

	protected static final String IDML_FILE_NAME = "idml.xml";

	@Autowired
	private CollectSurveyIdmlBinder binder;
	
	public CollectSurvey extractSurvey(File file) throws IdmlParseException, IOException {
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		ZipEntry zipEntry = getIdmEntry(zipEntries);
		InputStream is = zipFile.getInputStream(zipEntry);
		CollectSurvey survey = (CollectSurvey) binder.unmarshal(is);
		return survey;
	}
	
	private ZipEntry getIdmEntry(Enumeration<? extends ZipEntry> zipEntries) {
		while ( zipEntries.hasMoreElements() ) {
			ZipEntry zipEntry = zipEntries.nextElement();
			String entryName = zipEntry.getName();
			if ( ! zipEntry.isDirectory() && IDML_FILE_NAME.equals(entryName)  ) {
				return zipEntry;
			}
		}
		return null;
	}

}

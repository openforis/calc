/**
 * 
 */
package org.openforis.calc.engine;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.openforis.commons.versioning.Version;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Survey backup extractor
 * 
 * @author Mino Togna
 * 
 */
public class SurveyBackupExtractor {

	public static final String WORKSPACE_FILE_NAME 	= "workspace.json";
	public static final String VERSION_FILE_NAME 	= "calc-version.txt";
	public static final String DEV_VERSION 			= "PROJECT_VERSION";
	
	private ObjectMapper jsonObjectMapper;
	private ZipFile zipFile;

	/**
	 * @throws IOException
	 * @throws ZipException
	 * 
	 */
	public SurveyBackupExtractor( File file , ObjectMapper jsonObjectMapper ) throws ZipException, IOException{
		this.zipFile = new ZipFile(file);
		this.jsonObjectMapper = jsonObjectMapper;
	}

	private String extractZipEntry( String entryName ) throws IOException {
		ZipEntry entry = zipFile.getEntry(entryName);
		if ( entry == null ) {
			throw new IllegalStateException(entryName + " not found");
		}
		InputStream inputStream = zipFile.getInputStream(entry);

		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		inputStream.close();

		byte[] base64 = decodeBase64(writer.toString());
		return new String(base64);
	}

	public Version extractVersion() throws IOException {
		String versionString = extractZipEntry( VERSION_FILE_NAME );
		if( DEV_VERSION.equals(versionString) ){
			versionString = "0.0";
		}
		Version version = new Version(versionString);
		return version;
	}

	public Workspace extractWorkspace() throws IOException {
		Workspace workspace = null;
		if ( this.jsonObjectMapper != null ) {
			String wsString = extractZipEntry( WORKSPACE_FILE_NAME );
			workspace = this.jsonObjectMapper.readValue(wsString, Workspace.class);
		}
		return workspace;
	}

}

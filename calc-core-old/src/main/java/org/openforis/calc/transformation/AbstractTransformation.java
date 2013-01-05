/**
 * 
 */
package org.openforis.calc.transformation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

/**
 * @author Mino Togna
 * 
 */
abstract class AbstractTransformation {

	private static final String FILE_EXTENSION = ".ktr";

	static {
		try {
			KettleEnvironment.init();
		} catch ( KettleException e ) {
			throw new RuntimeException("Unable to initialize Kettle environment", e);
		}
	}

	// private Trans trans;
	private String transformationFile;

	AbstractTransformation(String transformationFile, String inputFileName) {
		this.transformationFile = transformationFile;
		createTransformationFile(inputFileName);
		// createTransformation(transformationFile);
	}

	public void execute(String[] arguments) {
		execute(arguments, true);
	}

	public void execute(String[] arguments, boolean waitUnitlFinished) {
		Trans trans = null;
		try {
			trans = createTransformation(transformationFile);
			trans.execute(arguments);
			if ( waitUnitlFinished ) {
				trans.waitUntilFinished();
			}
			if ( trans.getErrors() > 0 ) {
				throw new RuntimeException("There were errors during transformation execution.");
			}
		} catch ( KettleException e ) {
			throw new RuntimeException("Error while executing Kettle transformation", e);
		} finally {
			if ( trans != null ) {
				trans.cleanup();
			}
			trans = null;
		}
	}

	// public int getErrors() {
	// return trans.getErrors();
	// }

	private Trans createTransformation(String transformationFile) {
		try {
			TransMeta meta = new TransMeta(transformationFile);
			// this.
			Trans trans = new Trans(meta);
			return trans;
		} catch ( KettleXMLException e ) {
			throw new RuntimeException("Unable to create Kettle transformation", e);
		}
	}

	private void createTransformationFile(String inputFileName) {
		try {
			String name = this.getClass().getName();
			name = name.replaceAll("\\.", "/");
			name += FILE_EXTENSION;
			// name += "/" + getName();

			InputStream is = getClass().getClassLoader().getResourceAsStream(name);
			StringWriter sw = new StringWriter();
			IOUtils.copy(is, sw, "UTF-8");
			is.close();

			String string = sw.toString();
			string = string.replaceAll("\\$\\{INPUT_FILE_NAME}", inputFileName);

			File file = new File(transformationFile);
			FileUtils.write(file, string, Charset.forName("UTF-8"));
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to create transformation", e);
		}
	}

}

/**
 * 
 */
package org.openforis.calc.bi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.calc.web.context.CalcContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mino Togna
 * 
 */
public class SchemaManager {

	private static final String schemaResource = "bi/schema.xml";

	@Autowired
	private CalcContext calcContext;

	private boolean initialize;

	/**
	 * 
	 */
	public void initSchema() {
		try {
			if ( !initialize ) {
				String savedSchema = saveSchema();
				saveSchemaDataSource(savedSchema);

				initialize = true;
			}
		} catch ( IOException e ) {
			throw new RuntimeException("Error while creating mondrian schema", e);
		}
	}

	private void saveSchemaDataSource(String schemaPath) throws IOException, FileNotFoundException {
		String ds = getStringFromResource("saiku-ds/calc-results");
		File saikuDataSourceDir = getSaikuDataSourceDir();
		File dsFile = new File(saikuDataSourceDir, "calc-results");
		ds = ds.replaceAll("\\$\\{CATALOG_FILE}", schemaPath);

		FileUtils.write(dsFile, ds, Charset.forName("UTF-8"));
	}

	/**
	 * Return the absolute path of the xml schema file saved to disk
	 * 
	 * @return
	 * @throws IOException
	 */
	private String saveSchema() throws IOException {
		File dir = getSchemaDir();
		File schemaFile = new File(dir, "schema.xml");
		String schema = getStringFromResource(schemaResource);

		FileUtils.write(schemaFile ,schema, Charset.forName("UTF-8"));

		return schemaFile.getAbsolutePath();
	}

	private File getSaikuDataSourceDir() {
		String path = calcContext.getRealPath("../saiku/WEB-INF/classes/saiku-datasources");
		return new File(path);
	}

	private File getSchemaDir() {
		String path = calcContext.getRealPath("WEB-INF/bi-schema");
		return new File(path);
	}

	private String getStringFromResource(String res) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(res);
		StringWriter sw = new StringWriter();
		IOUtils.copy(stream, sw);
		String ds = sw.toString();
		stream.close();
		return ds;
	}
}

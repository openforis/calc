package org.openforis.calc.saiku;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.mondrian.Schema;
import org.openforis.calc.r.RScript;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Saiku properties class
 * @author Mino Togna
 *
 */
@Component
public class Saiku {

	@Value("${saiku.home}") 
	private String saikuHome;

	@Value("${calc.jdbc.username}")
	private String user;
	@Value("${calc.jdbc.password}")
	private String password;
	@Value("${calc.jdbc.url}")
	private String jdbcUrl;
	
	private String dataSource;
	private File classesFolder;
	private File dataSourcesFolder;

	@PostConstruct
	public void postConstruct() {
		InputStream stream = RScript.class.getClassLoader().getResourceAsStream( "org/openforis/calc/saiku/datasource.properties" );
		try {
			dataSource = IOUtils.toString( stream );
			classesFolder = new File( saikuHome , "WEB-INF/classes" );
			dataSourcesFolder = new File( classesFolder , "saiku-datasources" );
		} catch (IOException e) {
			throw new IllegalStateException("unable to find  org/openforis/calc/saiku/datasource.properties", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}
	
	/**
	 * Publishes the given schema into saiku repository.
	 * 1. write mondrian schema
	 * 2. wrtite saiku datasource
	 * 
	 * @param workspace
	 * @param schema
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void publishSchema(Workspace workspace, Schema schema) throws IOException, JAXBException {
		writeSchema(workspace, schema);
		
		writeDataSource(workspace);
	}
	
	public void writeSchema(Workspace workspace, Schema schema) throws IOException, JAXBException {
		// write xml file
		JAXBContext jaxbContext = JAXBContext.newInstance(Schema.class);
		
		Marshaller marshaller 	= jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		
		File mdxPath = getMdxDirectory(workspace);
		String wsName = workspace.getName();
		File f = new File( mdxPath, wsName+".xml" );
		if ( f.exists() ) {
			f.delete();
		}
		marshaller.marshal( schema, f );
	}
	
	public void writeDataSource(Workspace workspace) throws IOException {
		String wsName = workspace.getName();
		
		// write datasource file
//		String string = this.dataSource;
		String string = dataSource.replaceAll( "\\$\\{calc.jdbc.url}", jdbcUrl );
		string = string.replaceAll( "\\$\\{calc.jdbc.username}", user );
		string = string.replaceAll( "\\$\\{calc.jdbc.password}", password );
		
		string = string.replaceAll( "\\$\\{saiku.datasource.catalog}", String.format( "%s/%s.xml", wsName, wsName) );
		string = string.replaceAll( "\\$\\{saiku.datasource.name}", wsName );

		File dataSourceFile = new File( dataSourcesFolder , wsName );
		if( !dataSourceFile.exists() ){
			dataSourceFile.createNewFile();
		}

		BufferedOutputStream dataSourceFileStream = new BufferedOutputStream( new FileOutputStream(dataSourceFile) );
		IOUtils.write( string, dataSourceFileStream );
		IOUtils.closeQuietly( dataSourceFileStream );
	}
	
	
	public String getDataSource() {
		return dataSource;
	}
	
	public File getClassesFolder() {
		return classesFolder;
	}
	
	public File getDataSourcesFolder() {
		return dataSourcesFolder;
	}
	
	public File getMdxDirectory(Workspace workspace){
		String dir = String.format( "WEB-INF/classes/%s", workspace.getName() ); 
		File mdxPath = new File( this.saikuHome , dir );
		if( !mdxPath.exists() ){
			mdxPath.mkdirs();
		}
		return mdxPath;
	}
}
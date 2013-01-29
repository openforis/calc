/**
 * 
 */
package org.openforis.calc.transformation;

import javax.annotation.PostConstruct;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Mino Togna
 *
 */
@Component
public class TransformationDatabase {

	private static final String DB_META_NAME = "calcDb";
	
	@Value("${calc.jdbc.host}")
	private String host;
	@Value("${calc.jdbc.port}")
	private String port;
	@Value("${calc.jdbc.db}")
	private String db;
	@Value("${calc.jdbc.username}")
	private String user;
	@Value("${calc.jdbc.password}")
	private String pass;
//	@Value("${calc.jdbc.schema}")
//	private String schema;
	private DatabaseMeta databaseMeta;
	
	public TransformationDatabase() {
		
	}
	
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}
	
	@PostConstruct
	protected void init() throws KettleException{
		KettleEnvironment.init();
		databaseMeta = new DatabaseMeta(DB_META_NAME, "postgreSQL", null, host, db, port, user, pass);
	} 
}

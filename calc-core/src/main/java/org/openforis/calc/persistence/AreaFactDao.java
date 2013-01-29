package org.openforis.calc.persistence;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.transformation.InputDataTransformation;
import org.openforis.calc.transformation.OutputDataTransformation;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@Component
@Transactional
public class AreaFactDao {

	// extends JooqDaoSupport<AoiRecord, Aoi> {
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
	private DatabaseMeta dbMeta;

	public AreaFactDao() {
		super();
	}

	synchronized 
	public void createOrUpdateAreaFactTable(FlatDataStream data, String schema, String tableName) {

		try {
			TransMeta transMeta = new TransMeta();

			transMeta.addDatabase( dbMeta );
			transMeta.setName("importResults");

			Database database = getDatabase(transMeta);

			// 1. drop table
			dropResultsTable(transMeta, schema, tableName, database);

			// 2. read data in memory and create transformation
			InputDataTransformation inputDataTransformation = new InputDataTransformation(data);
			StepMeta inputStep = inputDataTransformation.getStepMeta();

			transMeta.addStep(inputStep);

			OutputDataTransformation outputDataTransformation = new OutputDataTransformation(dbMeta, schema, tableName);
			StepMeta outputStep = outputDataTransformation.getStepMeta();
			transMeta.addStep(outputStep);

			TransHopMeta hop = new TransHopMeta(inputStep, outputStep);
			transMeta.addTransHop(hop);

			// 3. create table

			String sqlStatementsString = transMeta.getSQLStatementsString();
			database.connect();
			database.execStatements(sqlStatementsString);
			database.disconnect();
			// System.err.println(sqlStatementsString);

			// 4. exec transformation
			Trans trans = new Trans(transMeta);
			trans.execute(null);
			trans.waitUntilFinished();
		} catch ( KettleException e ) {
			throw new PersistenceException("Error while importing estimation results", e);
		} catch ( IOException e ) {
			throw new PersistenceException("Error while reading data input", e);
		}

	}

	// public FlatDataStream streamByHierarchyName(String[] fieldNames, String hierarchyName) {
	// Field<?>[] fields = getFields(fieldNames);
	// if(fields == null || fields.length == 0) {
	// fields = AOI.getFields().toArray(new Field[AOI.getFields().size()]);
	// }
	// Factory create = getJooqFactory();
	//
	// Result<?> result =
	// create
	// .select(fields)
	// .from(AOI)
	// .join(AOI_HIERARCHY)
	// .on(AOI.AOI_HIERARCHY_ID.eq(AOI_HIERARCHY.AOI_HIERARCHY_ID))
	// .where(AOI_HIERARCHY.AOI_HIERARCHY_NAME.eq(hierarchyName))
	// .fetch();
	//
	// return stream( result );
	// }

	@PostConstruct
	public void init() throws KettleException {
		KettleEnvironment.init();
		dbMeta = new DatabaseMeta(DB_META_NAME, "postgreSQL", null, host, db, port, user, pass);
	}
	
	private Database dropResultsTable(TransMeta transMeta, String schema, String tableName, Database database) throws KettleDatabaseException {
		database.connect();
		String dropTableSql = "drop table if exists " + schema + "." + tableName + " cascade";
		database.execStatement(dropTableSql);
		database.disconnect();
		return database;
	}

	private Database getDatabase(TransMeta transMeta) {
		Database database = new Database(transMeta.getParent(), transMeta.findDatabase(DB_META_NAME));
		return database;
	}
}

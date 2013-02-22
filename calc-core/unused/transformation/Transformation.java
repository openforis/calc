/**
 * 
 */
package org.openforis.calc.transformation;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * @author M. Togna
 * 
 */
@Deprecated
public class Transformation {
	private static final String DB_META_NAME = "calcDb";

	// private DatabaseMeta databaseMeta;
	private TransMeta transMeta;
	private AbstractTransformationStep lastStep;

	public Transformation(TransformationDatabase transformationDatabase, String name) {
		// this.databaseMeta = databaseMeta;
		transMeta = new TransMeta();
		transMeta.setName(name);
		transMeta.addDatabase(transformationDatabase.getDatabaseMeta());
	}

	public void executeSql(String sql) throws KettleDatabaseException {
		Database database = getDatabase();
		database.connect();
		database.execStatement(sql);
		database.disconnect();
	}

	public void addStep(AbstractTransformationStep transformationStep) {
		StepMeta stepMeta = transformationStep.getStepMeta();
		this.transMeta.addStep(stepMeta);
		if ( lastStep != null ) {
			TransHopMeta hop = new TransHopMeta(lastStep.getStepMeta(), stepMeta);
			transMeta.addTransHop(hop);
		}
		lastStep = transformationStep;
	}

	public void executeAndWaitUntilFinished() throws KettleException {
		execute(true);
	}

	public void execute() throws KettleException {
		execute(false);
	}

	public void executeSQLStatementsString() throws KettleDatabaseException, KettleStepException {
		String sql = transMeta.getSQLStatementsString();
		executeSql(sql);
	}

	synchronized 
	private void execute(boolean waitUntiFinished) throws KettleException {
		Trans trans = new Trans(transMeta);
		trans.execute(null);
		if ( waitUntiFinished ) {
			trans.waitUntilFinished();
		}
	}

	private Database getDatabase() {
		Database database = new Database(transMeta.getParent(), transMeta.findDatabase(DB_META_NAME));
		return database;
	}
}

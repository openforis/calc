package org.openforis.calc.collect;

import javax.sql.DataSource;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.schema.Schemas;
import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 * TODO merge with COllect job
 */
public class CollectBackupImportJob extends CollectJob {

	@Override
	public String getName() {
		return "Collect data import";
	}

	public CollectBackupImportJob(Workspace workspace, DataSource dataSource, CollectSurvey survey) {
		super(workspace, dataSource, survey);
	}

	public void refreshWorkspace(Workspace ws) {
		setWorkspace(ws);
		setSchemas( new Schemas(ws) );
	}
}

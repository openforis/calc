package org.openforis.calc.collect;

import javax.sql.DataSource;

import org.openforis.calc.engine.Workspace;
import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectDataImportJob extends CollectJob {

	@Override
	public String getName() {
		return "Collect data import";
	}
	
	public CollectDataImportJob(Workspace workspace, DataSource dataSource,
			CollectSurvey survey) {
		super(workspace, dataSource, survey);
	}

}

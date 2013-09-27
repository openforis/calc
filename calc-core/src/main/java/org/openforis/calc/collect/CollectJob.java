package org.openforis.calc.collect;

import javax.sql.DataSource;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Workspace;
import org.openforis.collect.model.CollectSurvey;

public class CollectJob extends Job {

	private CollectSurvey survey;

	public CollectJob(Workspace workspace, DataSource dataSource, CollectSurvey survey) {
		super(workspace, dataSource);
		this.survey = survey;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
}

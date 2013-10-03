package org.openforis.calc.collect;

import javax.sql.DataSource;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Workspace;
import org.openforis.collect.model.CollectSurvey;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 *
 */
public abstract class CollectJob extends Job {

	@JsonIgnore
	private CollectSurvey survey;

	public CollectJob(Workspace workspace, DataSource dataSource, CollectSurvey survey) {
		super(workspace, dataSource);
		this.survey = survey;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
}

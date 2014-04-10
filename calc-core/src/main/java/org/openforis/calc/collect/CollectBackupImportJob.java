package org.openforis.calc.collect;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.schema.Schemas;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author S. Ricci
 * TODO merge with COllect job
 */
@Component(value="CollectBackupImportJob")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CollectBackupImportJob extends Job {

	@JsonIgnore
	private CollectSurvey survey;

	@JsonIgnore
	private RelationalSchema inputRelationalSchema;

	@Override
	public String getName() {
		return "Collect data import";
	}

	@Override
	public void init() {
		super.init();
		inputRelationalSchema = createInputRelationalSchema();
	}
	
	public void refreshWorkspace(Workspace ws) {
		setWorkspace(ws);
		setSchemas( new Schemas(ws) );
	}

	private RelationalSchema createInputRelationalSchema() {
		String inputSchemaName = getWorkspace().getInputSchema();
		RelationalSchemaConfig config = RelationalSchemaConfig.createDefault();
		config.setDefaultCode("-1");
		config.setUniqueColumnNames(true);
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator(config);
		CollectSurvey survey = getSurvey();
		try {
			RelationalSchema schema = rdbGenerator.generateSchema(survey, inputSchemaName);
			return schema;
		} catch (CollectRdbException e) {
			throw new RuntimeException("Error generating relational input schema", e);
		}
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	protected RelationalSchema getInputRelationalSchema() {
		return inputRelationalSchema;
	}
	
}

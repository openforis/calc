package org.openforis.calc.collect;

import javax.sql.DataSource;

import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.schema.Schemas;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author S. Ricci
 * @author Mino Togna
 */
public class CollectSurveyImportJob extends Job {

	@JsonIgnore
	private CollectSurvey survey;

	@JsonIgnore
	private RelationalSchema inputRelationalSchema;

	public CollectSurveyImportJob(  Workspace workspace, DataSource dataSource, CollectSurvey survey  ){
		super(workspace, dataSource);
		this.survey = survey;
	}
	
	@Override
	public void init() {
		super.init();
		inputRelationalSchema = createInputRelationalSchema();
	}
	
	private RelationalSchema createInputRelationalSchema() {
		String inputSchemaName = getWorkspace().getInputSchema();
		RelationalSchemaConfig config = RelationalSchemaConfig.createDefault();
		config.setDefaultCode( "-1" );
		config.setUniqueColumnNames( true );
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator( config );
		CollectSurvey survey = getSurvey();
		try {
			RelationalSchema schema = rdbGenerator.generateSchema( survey, inputSchemaName );
			return schema;
		} catch ( CollectRdbException e ){
			throw new RuntimeException( "Error generating relational input schema" , e );
		}
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	protected RelationalSchema getInputRelationalSchema() {
		return inputRelationalSchema;
	}
	
	@Override
	public String getName() {
		return "Collect data import";
	}


	public void updateWorkspace(Workspace ws) {
		setWorkspace(ws);
		setSchemas( new Schemas(ws) );
	}
}

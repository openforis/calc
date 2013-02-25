package org.openforis.calc.olap.schema;

import org.openforis.calc.model.SurveyMetadata;

/**
 * @author G. Miceli
 */
public class SchemaGenerator {
	
	private SurveyMetadata surveyMetadata;

	public SchemaGenerator(SurveyMetadata surveyMetadata) {
		this.surveyMetadata = surveyMetadata;
	}
	
	public SurveyMetadata getSurveyMetadata() {
		return surveyMetadata;
	}
	
	public Schema generateSchema() {
		// TODO refactor schema generation
		return new Schema(surveyMetadata);
	}
}
